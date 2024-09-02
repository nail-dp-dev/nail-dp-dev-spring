package com.backend.naildp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostBoundaryRequest;
import com.backend.naildp.dto.post.PostInfoResponse;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TagRequestDto;
import com.backend.naildp.dto.post.TempPostRequestDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.TagRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UsersProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	private final TagPostRepository tagPostRepository;
	private final PhotoRepository photoRepository;
	private final FollowRepository followRepository;
	private final UsersProfileRepository usersProfileRepository;
	private final S3Service s3Service;
	private final PostDeletionFacade postDeletionFacade;

	@Transactional
	public void uploadPost(String nickname, PostRequestDto postRequestDto, List<MultipartFile> files) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (files == null || files.isEmpty()) {
			throw new CustomException("Not Input File", ErrorCode.FILE_EXCEPTION);
		}

		if (files.size() > 10) {
			throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INVALID_FORM);
		}

		Post post = Post.builder()
			.user(user)
			.postContent(postRequestDto.getPostContent())
			.boundary(postRequestDto.getBoundary())
			.tempSave(false)
			.build();

		postRepository.save(post);

		List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(files);

		List<TagRequestDto> tags = postRequestDto.getTags();
		for (TagRequestDto tag : tags) {
			String tagName = tag.getTagName().toLowerCase();
			Tag existingTag = tagRepository.findByName(tagName)
				.orElseGet(() -> tagRepository.save(new Tag(tagName)));
			tagPostRepository.save(new TagPost(existingTag, post));
		}
		fileRequestDtos.stream().map(fileRequestDto -> new Photo(post, fileRequestDto)).forEach(photoRepository::save);
	}

	@Transactional
	public void editPost(String nickname, PostRequestDto postRequestDto, List<MultipartFile> files, Long postId) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		validateUser(post, nickname);

		if (getSize(files) + getSize(post.getPhotos()) - getSize(postRequestDto.getDeletedFileUrls()) > 10) {
			throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INVALID_FORM);

		}

		// 게시물에는 file 필수 -> deleted url 개수 = 저장된 url 개수 같으면, 새로운 file을 꼭 받아야함.
		if (getSize(post.getPhotos()) == getSize(postRequestDto.getDeletedFileUrls()) && (files == null
			|| files.isEmpty())) {
			throw new CustomException("파일을 첨부해주세요.", ErrorCode.INVALID_FORM);
		}
		post.update(postRequestDto);

		tagPostRepository.deleteAllByPostId(postId);

		updateTagsAndFiles(postRequestDto.getTags(), files, post);

		deleteFileUrls(postRequestDto.getDeletedFileUrls());

	}

	// 게시물 수정 조회
	@Transactional(readOnly = true)
	public EditPostResponseDto getEditingPost(String nickname, Long postId) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		validateUser(post, nickname);

		List<Photo> photos = photoRepository.findAllByPostId(postId);

		List<String> tagNames = post.getTagPosts()
			.stream()
			.map(tagPost -> tagPost.getTag().getName())
			.collect(Collectors.toList());

		List<FileRequestDto> fileRequestDtos = photos.stream().map(FileRequestDto::new).toList();

		return EditPostResponseDto.builder()
			.postContent(post.getPostContent())
			.tags(tagNames)
			.photos(fileRequestDtos)
			.tempSave(post.getTempSave())
			.boundary(post.getBoundary())
			.build();
	}

	@Transactional
	public void tempSavePost(String nickname, TempPostRequestDto tempPostRequestDto, List<MultipartFile> files) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Optional<Post> postOptional = postRepository.findPostByTempSaveIsTrueAndUserNickname(nickname);
		Post post;

		if (postOptional.isPresent()) {

			post = postOptional.get();

			if (getSize(files) + getSize(post.getPhotos()) - getSize(tempPostRequestDto.getDeletedFileUrls()) > 10) {
				throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INVALID_FORM);
			}

			if (isRequestEmpty(tempPostRequestDto, files)) {
				if (getSize(post.getPhotos()) == getSize(tempPostRequestDto.getDeletedFileUrls())) {
					throw new CustomException("임시저장 할 내용이 없습니다.", ErrorCode.INVALID_FORM);
				}
			}

			post.tempUpdate(tempPostRequestDto);
			tagPostRepository.deleteAllByPostId(postOptional.get().getId());
			deleteFileUrls(tempPostRequestDto.getDeletedFileUrls());

		} else {

			if (getSize(files) > 10) {
				throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INVALID_FORM);
			}

			if (isRequestEmpty(tempPostRequestDto, files)) {
				throw new CustomException("임시저장 할 내용이 없습니다.", ErrorCode.INVALID_FORM);
			}

			post = Post.builder()
				.user(user)
				.postContent(tempPostRequestDto.getPostContent())
				.boundary(tempPostRequestDto.getBoundary())
				.tempSave(true)
				.build();
			postRepository.save(post);
		}

		updateTagsAndFiles(tempPostRequestDto.getTags(), files, post);
	}

	/**
	 * /posts/{postId}
	 * 특정 게시물 상세정보 읽기 API
	 */
	@Transactional(readOnly = true)
	public PostInfoResponse postInfo(String nickname, Long postId) {
		// post - writer 정보 가져오기
		Post post = postRepository.findPostAndWriterById(postId)
			.orElseThrow(() -> new CustomException("게시물을 조회할 수 없습니다.", ErrorCode.NOT_FOUND));
		User writer = post.getUser();

		// 읽기 권한 확인
		boolean followingStatus = isFollower(nickname, writer, post.getBoundary());
		int followerCount = followRepository.countFollowersByUserNickname(writer.getNickname());

		// 태그 TagPost - Tag 조회
		List<TagPost> tagPosts = tagPostRepository.findTagPostAndTagByPost(post);
		List<Tag> tags = tagPosts.stream().map(TagPost::getTag).collect(Collectors.toList());

		return PostInfoResponse.of(post, nickname, followingStatus, followerCount, tags);
	}

	@Transactional
	public void changeBoundary(Long postId, PostBoundaryRequest postBoundaryRequest, String username) {
		//요청한 사용자가 게시글 작성자인지 확인 필요
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (post.notWrittenBy(username)) {
			throw new CustomException("게시글 범위 설정은 작성자만 할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}

		//변경
		post.changeBoundary(postBoundaryRequest);
	}

	@Transactional
	public void deletePost(Long postId, String nickname) {

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (post.notWrittenBy(nickname)) {
			throw new CustomException("게시글 삭제는 작성자만 할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}
		postDeletionFacade.deletePostAndAssociations(postId);

	}

	@Transactional(readOnly = true)
	public Long countSharing(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		// post 에 접근할 수 있는지 확인 필요
		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username,
			post.getUser()) && post.notWrittenBy(username)) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		return post.getSharing();
	}

	@Transactional
	public Long sharePost(Long postId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물은 공유할 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물은 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username,
			post.getUser()) && post.notWrittenBy(username)) {
			throw new CustomException("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		post.share();

		return post.getId();
	}

	private void deleteFileUrls(List<String> deletedFileUrls) {
		if (deletedFileUrls == null || deletedFileUrls.isEmpty()) {
			return;
		}

		// 데이터베이스에서 URL 리스트에 해당하는 사진들 조회
		List<Photo> photos = photoRepository.findByPhotoUrlIn(deletedFileUrls);

		// 사진 삭제 및 S3 파일 삭제
		photos.forEach(photo -> {
			String fileUrl = photo.getPhotoUrl();
			photoRepository.delete(photo);
			s3Service.deleteFile(fileUrl);
		});

	}

	private void updateTagsAndFiles(List<TagRequestDto> tags, List<MultipartFile> files, Post post) {
		if (tags != null && !tags.isEmpty()) {
			for (TagRequestDto tag : tags) {
				String tagName = tag.getTagName().toLowerCase();
				Tag existingTag = tagRepository.findByName(tagName)
					.orElseGet(() -> tagRepository.save(new Tag(tagName)));
				tagPostRepository.save(new TagPost(existingTag, post));
			}
		}

		if (files != null && !files.isEmpty()) {
			List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(files);
			fileRequestDtos.stream()
				.map(fileRequestDto -> new Photo(post, fileRequestDto))
				.forEach(photoRepository::save);
		}
	}

	private boolean isFollower(String nickname, User writer, Boundary boundary) {
		//not equal
		//	all & follow -> exists
		//  none -> exception
		//equal -> false
		if (equalsReaderAndWriter(nickname, writer)) {
			return false;
		}

		if (boundary == Boundary.NONE) {
			throw new CustomException("게시물을 읽을 수 없습니다.", ErrorCode.NOT_FOUND);
		}

		return followRepository.existsByFollowerNicknameAndFollowing(nickname, writer);
	}

	private boolean equalsReaderAndWriter(String nickname, User writer) {
		return writer.equalsNickname(nickname);
	}

	private boolean isRequestEmpty(TempPostRequestDto tempPostRequestDto, List<MultipartFile> files) {
		return tempPostRequestDto.getPostContent().isBlank()
			&& tempPostRequestDto.getTags().isEmpty()
			&& (files == null || files.isEmpty());
	}

	private int getSize(List<?> list) {
		return list == null ? 0 : list.size();
	}

	private void validateUser(Post post, String nickname) {
		if (!post.getUser().getNickname().equals(nickname)) {
			throw new CustomException("본인이 작성한 게시글만 수정할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}
	}

}
