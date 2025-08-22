package com.backend.naildp.service.post;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Component("new")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewestPostStrategy implements PostStrategy {

	private final PostRepository postRepository;

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> newestPostSlice = postRepository.findNewestPostSlice(username, cursorPostId, pageRequest);

		if (newestPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		if (!StringUtils.hasText(username)) {
			return new PostSummaryResponse(newestPostSlice);
		}

		List<Post> likedPosts = postRepository.findLikedPosts(username);
		List<Post> savedPosts = postRepository.findPostsInArchive(username);
		return new PostSummaryResponse(newestPostSlice, savedPosts, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> newestPostSlice = postRepository.findNewestPostSlice(username, cursorPostId, pageRequest);

		if (newestPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		if (!StringUtils.hasText(username)) {
			return new PostSummaryResponse(newestPostSlice);
		}

		List<Post> likedPosts = postRepository.findLikedPosts(username);
		List<Post> savedPosts = postRepository.findPostsInArchive(username);
		return new PostSummaryResponse(newestPostSlice, savedPosts, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username) {
		return homePostsV2(size, cursorPostId, username);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoinAndFollow(int size, Long cursorPostId, String username) {
		return homePostsV2(size, cursorPostId, username);
	}

	@Override
	public PostSummaryResponse homePostsV3(int size, Long cursorPostId, String username) {
		return homePostsV2(size, cursorPostId, username);
	}
}
