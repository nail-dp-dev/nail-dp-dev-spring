package com.backend.naildp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.Boundary;
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
import com.backend.naildp.repository.FollowRepository;
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
	private final FollowRepository followRepository;

	public PostSummaryResponse homePosts(String choice, int size, long cursorPostId, String nickname) {
		PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

		if (!StringUtils.hasText(nickname)) {
			Slice<Post> recentPosts = getRecentOpenedPosts(cursorPostId, pageRequest);
			return new PostSummaryResponse(recentPosts);
		}

		List<User> followingUser = followRepository.findFollowingUserByFollowerNickname(nickname);
		Slice<Post> recentPosts = getRecentPosts(cursorPostId, followingUser, pageRequest);

		if (recentPosts.isEmpty()) {
			log.debug("최신 게시물이 하나도 없습니다.");
			throw new CustomException("게시물이 없습니다.", ErrorCode.FILES_NOT_REGISTERED);
		}

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream().map(ArchivePost::getPost).collect(Collectors.toList());

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());

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

	public PostSummaryResponse findLikedPost(String nickname, int pageSize, long cursorId) {
		PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

		// 좋아요한 게시글 조회
		List<User> followingUsers = followRepository.findFollowingUserByFollowerNickname(nickname);
		Slice<PostLike> postLikes = getOpenedPostLikes(cursorId, nickname, followingUsers, pageRequest);
		Slice<Post> likedPosts = postLikes.map(PostLike::getPost);

		// 게시글 저장 여부 체크
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		return new PostSummaryResponse(likedPosts, savedPosts);
	}

	private Slice<PostLike> getOpenedPostLikes(long cursorPostLikeId, String nickname, List<User> followingUsers,
		PageRequest pageRequest) {
		if (isFirstPage(cursorPostLikeId)) {
			return postLikeRepository.findPostLikesByFollowing(nickname, followingUsers, pageRequest);
		}
		return postLikeRepository.findPostLikesByIdAndFollowing(nickname, cursorPostLikeId, followingUsers,
			pageRequest);
	}

	private Slice<Post> getRecentOpenedPosts(long cursorPostId, PageRequest pageRequest) {
		if (isFirstPage(cursorPostId)) {
			return postRepository.findPostsByBoundaryAndTempSaveFalse(Boundary.ALL, pageRequest);
		}
		return postRepository.findPostsByIdBeforeAndBoundaryAndTempSaveFalse(cursorPostId, Boundary.ALL,
			pageRequest);
	}

	private Slice<Post> getRecentPosts(long cursorPostId, List<User> followingUser, PageRequest pageRequest) {
		if (isFirstPage(cursorPostId)) {
			return postRepository.findRecentPostsByFollowing(followingUser, pageRequest);
		}
		return postRepository.findRecentPostsByIdAndFollowing(cursorPostId, followingUser, pageRequest);
	}

	private boolean isFirstPage(long cursorPostId) {
		return cursorPostId == -1L;
	}
}
