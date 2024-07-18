package com.backend.naildp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public PostSummaryResponse homePosts(String choice, int size, long cursorPostId, String nickname) {
		log.info("PostService#homePosts 실행");
		PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdDate"));
		Slice<Post> recentPosts;
		if (cursorPostId == -1L) {
			recentPosts = postRepository.findPostsByBoundaryNotAndTempSaveFalse(Boundary.NONE, pageRequest);
		} else {
			recentPosts = postRepository.findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(cursorPostId,
				Boundary.NONE, pageRequest);
		}
		log.info("PostService#homePosts - 게시물 페이징으로 가져오기");

		if (recentPosts.isEmpty()) {
			log.debug("최신 게시물이 하나도 없습니다.");
			throw new CustomException("게시물이 없습니다.", ErrorCode.FILES_NOT_REGISTERED);
		}

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());
		log.info("PostService#homePosts - 저장한 게시물 가져오기");

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());
		log.info("PostService#homePosts - 좋아요 체크한 게시물 가져오기");

		return new PostSummaryResponse(recentPosts, savedPosts, likedPosts);
	}

	@Transactional
	public ResponseEntity<ApiResponse<?>> uploadPost(String nickname, PostRequestDto postRequestDto,
		List<String> filePaths) {
		User user = userRepository.findByNickname(nickname)
			.orElseThrow(() -> new CustomException("nickname 으로 회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Post post = new Post(postRequestDto, user);
		postRepository.save(post);

		List<TagRequestDto> tags = postRequestDto.getTags();
		for (TagRequestDto tag : tags) {
			Tag existingTag = tagRepository.findByName(tag.getTagName())
				.orElseGet(() -> tagRepository.save(new Tag(tag.getTagName()))); //null 일 때 호출
			tagPostRepository.save(new TagPost(existingTag, post));

		}
		for (String filePath : filePaths) {
			Photo photo = new Photo(post, filePath);
			photoRepository.save(photo);

		}

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
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		return likedPost.map(post -> HomePostResponse.likedPostResponse(post, savedPosts));
	}
}
