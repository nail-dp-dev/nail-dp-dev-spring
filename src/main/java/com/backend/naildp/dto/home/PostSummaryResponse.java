package com.backend.naildp.dto.home;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {

	private Long oldestPostId;
	private Slice<HomePostResponse> homePostResponses;

	public PostSummaryResponse(Slice<Post> latestPosts, List<Post> savedPosts, List<Post> likedPosts) {
		oldestPostId = latestPosts.getContent().get(latestPosts.getSize() - 1).getId();
		homePostResponses = latestPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts));
	}
}
