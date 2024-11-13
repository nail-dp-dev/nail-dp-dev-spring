package com.backend.naildp.service.post;

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

	private PageRequest createPageRequest(int size, String property) {
		return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, property));
	}

	private Slice<PostLike> getOpenedPostLikes(long cursorPostLikeId, String nickname, List<User> followingUsers,
		PageRequest pageRequest) {
		if (isFirstPage(cursorPostLikeId)) {
			return postLikeRepository.findPostLikesByFollowing(nickname, followingUsers, pageRequest);
		}
		return postLikeRepository.findPostLikesByIdAndFollowing(nickname, cursorPostLikeId, followingUsers,
			pageRequest);
	}

	private boolean isFirstPage(long cursorPostId) {
		return cursorPostId == -1L;
	}
}
