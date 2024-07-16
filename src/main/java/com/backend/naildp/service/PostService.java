package com.backend.naildp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TagRequestDto;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.TagRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final PostLikeRepository postLikeRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	private final TagPostRepository tagPostRepository;
	private final PhotoRepository photoRepository;
	private final S3Service s3Service;

	public List<HomePostResponse> homePosts(String nickname) {
		PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<Post> recentPosts = postRepository.findPostsAndPhotoByBoundary(Boundary.ALL, pageRequest);

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream().map(ArchivePost::getPost).collect(Collectors.toList());

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());

		return recentPosts.stream()
			.map(post -> new HomePostResponse(post, savedPosts, likedPosts))
			.collect(Collectors.toList());
	}

	@Transactional
	public ResponseEntity<ApiResponse<?>> uploadPost(String nickname, PostRequestDto postRequestDto,
		List<MultipartFile> files) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = new Post(postRequestDto, user);
		postRepository.save(post);

		List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(files);

		List<TagRequestDto> tags = postRequestDto.getTags();
		for (TagRequestDto tag : tags) {
			Tag existingTag = tagRepository.findByName(tag.getTagName())
				.orElseGet(() -> tagRepository.save(new Tag(tag.getTagName()))); //null 일 때 호출
			tagPostRepository.save(new TagPost(existingTag, post));

		}
		fileRequestDtos.stream().map(fileRequestDto -> new Photo(post, fileRequestDto)).forEach(photoRepository::save);

		return ResponseEntity.ok().body(ApiResponse.successResponse(post, "게시글 작성이 완료되었습니다", 2001));
	}

	public Page<HomePostResponse> findLikedPost(String nickname, int pageNumber) {
		PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "createdDate"));

		// 좋아요한 게시글 조회
		Page<PostLike> postLikes = postLikeRepository.findPagedPostLikesByBoundaryOpened(pageRequest, nickname,
			Boundary.NONE);
		Page<Post> likedPost = postLikes.map(PostLike::getPost);

		// 게시글 저장 여부 체크
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream().map(ArchivePost::getPost).collect(Collectors.toList());

		return likedPost.map(post -> HomePostResponse.likedPostResponse(post, savedPosts));
	}

	@Transactional
	public ResponseEntity<ApiResponse<?>> editPost(String nickname, PostRequestDto postRequestDto,
		List<MultipartFile> files, Long postId) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		// 게시물에는 file 필수 -> deleted url 개수 = 저장된 url 개수 같으면, 새로운 file을 꼭 받아야함.
		if ((long)post.getPhotos().size() == postRequestDto.getDeletedFileUrls().size() && files.isEmpty()) {
			throw new CustomException("파일을 첨부해주세요.", ErrorCode.INPUT_NULL);
		}

		tagPostRepository.deleteAllByPostId(postId);

		List<TagRequestDto> tags = postRequestDto.getTags();
		for (TagRequestDto tag : tags) {
			Tag existingTag = tagRepository.findByName(tag.getTagName())
				.orElseGet(() -> tagRepository.save(new Tag(tag.getTagName()))); //null 일 때 호출
			tagPostRepository.save(new TagPost(existingTag, post));
		}
		post.update(postRequestDto);

		if (files != null) {
			List<FileRequestDto> fileRequestDtos = s3Service.saveFiles(files);
			fileRequestDtos.stream()
				.map(fileRequestDto -> new Photo(post, fileRequestDto))
				.forEach(photoRepository::save);
		}

		if (!postRequestDto.getDeletedFileUrls().isEmpty()) {
			postRequestDto.getDeletedFileUrls()
				.stream()
				.map(photoRepository::findByPhotoUrl)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(photo -> {
					String fileUrl = photo.getPhotoUrl();
					photoRepository.delete(photo);
					s3Service.deleteFile(fileUrl);
				});
		}

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "게시글 수정이 완료되었습니다", 2001));
	}

	// 게시물 수정 조회
	public ResponseEntity<ApiResponse<?>> getEditingPost(String nickname, Long postId) {

		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("해당 포스트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		List<Photo> photos = photoRepository.findAllByPostId(postId);

		List<String> tagNames = post.getTagPosts().stream()
			.map(tagPost -> tagPost.getTag().getName())
			.collect(Collectors.toList());

		List<FileRequestDto> fileRequestDtos = photos.stream()
			.map(FileRequestDto::new)
			.toList();

		EditPostResponseDto editPostResponseDto = EditPostResponseDto.builder()
			.postContent(post.getPostContent())
			.tags(tagNames)
			.photos(fileRequestDtos)
			.tempSave(post.getTempSave())
			.boundary(post.getBoundary())
			.build();

		return ResponseEntity.ok().body(ApiResponse.successResponse(editPostResponseDto, "수정 게시글 조회 완료", 2000));
		// 용량문제
	}

}
