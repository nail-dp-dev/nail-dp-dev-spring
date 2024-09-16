package com.backend.naildp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostInfoService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;

	public PostSummaryResponse homePosts(String choice, int size, long cursorPostId, String nickname) {
		PageRequest pageRequest = createPageRequest(size, "id");

		if (!StringUtils.hasText(nickname)) {
			log.info("익명사용자 응답");
			Slice<Post> recentPosts = getRecentOpenedPosts(cursorPostId, pageRequest);

			return recentPosts.isEmpty() ? PostSummaryResponse.createEmptyResponse() :
				PostSummaryResponse.createForAnonymous(recentPosts);
		}

		List<User> followingUser = followRepository.findFollowingUserByFollowerNickname(nickname);
		Slice<Post> recentPosts = getRecentPosts(cursorPostId, followingUser, pageRequest);

		if (recentPosts.isEmpty()) {
			log.info("최신 게시물이 없습니다.");
			return PostSummaryResponse.createEmptyResponse();
		}

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream().map(ArchivePost::getPost).collect(Collectors.toList());

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());

		return new PostSummaryResponse(recentPosts, savedPosts, likedPosts);
	}

	public PostSummaryResponse findLikedPost(String nickname, int pageSize, long cursorId) {
		PageRequest pageRequest = createPageRequest(pageSize, "createdDate");

		// 좋아요한 게시글 조회
		List<User> followingUsers = followRepository.findFollowingUserByFollowerNickname(nickname);
		Slice<PostLike> postLikes = getOpenedPostLikes(cursorId, nickname, followingUsers, pageRequest);
		Slice<Post> likedPosts = postLikes.map(PostLike::getPost);

		if (likedPosts.isEmpty()) {
			log.info("좋아요한 게시물이 없다.");
			return PostSummaryResponse.createEmptyResponse();
		}

		// 게시글 저장 여부 체크
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream().map(ArchivePost::getPost).collect(Collectors.toList());

		// return new PostSummaryResponse(likedPosts, savedPosts);
		return PostSummaryResponse.createLikedPostSummary(likedPosts, savedPosts);
	}

	private PageRequest createPageRequest(int size, String id) {
		return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, id));
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
		return postRepository.findPostsByIdBeforeAndBoundaryAndTempSaveFalse(cursorPostId, Boundary.ALL, pageRequest);
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
