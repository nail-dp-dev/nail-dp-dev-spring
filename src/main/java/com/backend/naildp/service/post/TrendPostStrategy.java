package com.backend.naildp.service.post;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.service.post.dto.UserContext;

import lombok.RequiredArgsConstructor;

@Component("trending")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrendPostStrategy implements PostStrategy {

	private final PostRepository postRepository;

	@Override
	public boolean isExecutable(UserContext userContext) {
		return true;
	}

	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		PageRequest pageRequest = PageRequest.of(0, size);

		Post cursorPost = findCursorPost(cursorPostId);
		Slice<Post> trendPostSlice = postRepository.findTrendPostSliceWithoutSubquery(username, cursorPost, pageRequest);

		// Slice<Post> trendPostSlice = postRepository.findTrendPostSlice(username, cursorPostId, pageRequest);

		if (trendPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		if (!StringUtils.hasText(username)) {
			return new PostSummaryResponse(trendPostSlice);
		}

		List<Post> likedPosts = postRepository.findLikedPosts(username);
		List<Post> savedPosts = postRepository.findPostsInArchive(username);
		return new PostSummaryResponse(trendPostSlice, savedPosts, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username) {
		PageRequest pageRequest = PageRequest.of(0, size);

		Post cursorPost = findCursorPost(cursorPostId);
		Slice<Post> trendPostSlice = postRepository.findTrendPostSliceWithoutSubquery(username, cursorPost, pageRequest);

		// Slice<Post> trendPostSlice = postRepository.findTrendPostSlice(username, cursorPostId, pageRequest);

		if (trendPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		if (!StringUtils.hasText(username)) {
			return new PostSummaryResponse(trendPostSlice);
		}

		List<Post> likedPosts = postRepository.findLikedPosts(username);
		List<Post> savedPosts = postRepository.findPostsInArchive(username);
		return new PostSummaryResponse(trendPostSlice, savedPosts, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username) {
		return homePostsV2(size, cursorPostId, username);
	}

	@Override
	public PostSummaryResponse homePostsFilteredByTagsAndFollowees(int size, Long cursorPostId, String username) {
		return homePostsV2(size, cursorPostId, username);
	}

	@Override
	public PostSummaryResponse homePostsV3(int size, Long cursorPostId, UserContext userContext) {
		return homePostsV2(size, cursorPostId, userContext.getUsername());
	}

	@Nullable
	private Post findCursorPost(Long cursorPostId) {
		return cursorPostId == null ? null : postRepository.findById(cursorPostId).orElse(null);
	}

}
