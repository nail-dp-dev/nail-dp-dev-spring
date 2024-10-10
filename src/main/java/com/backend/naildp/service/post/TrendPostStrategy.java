package com.backend.naildp.service.post;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Component("trend-post")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrendPostStrategy implements PostStrategy {

	private final PostRepository postRepository;

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> trendPostSlice = postRepository.findTrendPostSlice(username, cursorPostId, pageRequest);

		if (trendPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> likedPosts = postRepository.findLikedPosts(username);
		List<Post> savedPosts = postRepository.findPostsInArchive(username);
		return new PostSummaryResponse(trendPostSlice, savedPosts, likedPosts);
	}
}
