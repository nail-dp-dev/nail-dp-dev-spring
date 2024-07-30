package com.backend.naildp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
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
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.TagRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	private final TagPostRepository tagPostRepository;
	private final PhotoRepository photoRepository;
	private final S3Service s3Service;

	@Transactional
	public void uploadPost(String nickname, PostRequestDto postRequestDto, List<MultipartFile> files) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (files == null || files.isEmpty()) {
			throw new CustomException("Not Input File", ErrorCode.INPUT_NULL);
		}

		if (files.size() > 10) {
			throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INPUT_NULL);
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
			Tag existingTag = tagRepository.findByName(tag.getTagName())
				.orElseGet(() -> tagRepository.save(new Tag(tag.getTagName()))); //null 일 때 호출
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
			throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INPUT_NULL);

		}

		// 게시물에는 file 필수 -> deleted url 개수 = 저장된 url 개수 같으면, 새로운 file을 꼭 받아야함.
		if (getSize(post.getPhotos()) == getSize(postRequestDto.getDeletedFileUrls()) && (files == null
			|| files.isEmpty())) {
			throw new CustomException("파일을 첨부해주세요.", ErrorCode.INPUT_NULL);
		}
		post.update(postRequestDto);

		tagPostRepository.deleteAllByPostId(postId);

		updateTagsAndFiles(postRequestDto.getTags(), files, post);

		deleteFileUrls(postRequestDto.getDeletedFileUrls());

	}

	// 게시물 수정 조회
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

		Optional<Post> postOptional = postRepository.findPostByTempSaveIsTrueAndUser(user);
		Post post;

		if (postOptional.isPresent()) {

			post = postOptional.get();

			if (getSize(files) + getSize(post.getPhotos()) - getSize(tempPostRequestDto.getDeletedFileUrls()) > 10) {
				throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INPUT_NULL);
			}

			if (isRequestEmpty(tempPostRequestDto, files)) {
				if (getSize(post.getPhotos()) == getSize(tempPostRequestDto.getDeletedFileUrls())) {
					throw new CustomException("임시저장 할 내용이 없습니다.", ErrorCode.INPUT_NULL);
				}
			}

			post.tempUpdate(tempPostRequestDto);
			tagPostRepository.deleteAllByPostId(postOptional.get().getId());
			deleteFileUrls(tempPostRequestDto.getDeletedFileUrls());

		} else {

			if (getSize(files) > 10) {
				throw new CustomException("업로드 가능한 파일 수는 10개 입니다.", ErrorCode.INPUT_NULL);
			}

			if (isRequestEmpty(tempPostRequestDto, files)) {
				throw new CustomException("임시저장 할 내용이 없습니다.", ErrorCode.INPUT_NULL);
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
				Tag existingTag = tagRepository.findByName(tag.getTagName())
					.orElseGet(() -> tagRepository.save(new Tag(tag.getTagName())));
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

	// /**
	//  * /posts/{postId}
	//  * 특정 게시물 상세정보 읽기 API
	//  */
	// public PostInfoResponse postInfo(String nickname, Long postId) {
	// 	// post - writer 정보 가져오기
	// 	Post post = postRepository.findPostAndWriterById(postId)
	// 		.orElseThrow(() -> new CustomException("게시물을 조회할 수 없습니다.", ErrorCode.NOT_FOUND));
	// 	User writer = post.getUser();
	// 	Profile profile = profileRepository.findProfileUrlByThumbnailIsTrueAndUser(writer)
	// 		.orElseThrow(() -> new CustomException("작성자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
	//
	// 	// 읽기 권한 확인
	// 	boolean followingStatus = isFollower(nickname, writer, post.getBoundary());
	// 	int followerCount = followRepository.countFollowersByUserNickname(writer.getNickname());
	//
	// 	// 태그 TagPost - Tag 조회
	// 	List<TagPost> tagPosts = tagPostRepository.findTagPostAndTagByPost(post);
	// 	List<Tag> tags = tagPosts.stream().map(TagPost::getTag).collect(Collectors.toList());
	//
	// 	return PostInfoResponse.of(post, writer, profile, followingStatus, followerCount, tags);
	// }

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
