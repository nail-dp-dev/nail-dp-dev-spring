package com.backend.naildp.dto.home;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {

	private Long oldestPostId;
	private Slice<HomePostResponse> postSummaryList;

	public PostSummaryResponse(Slice<Post> latestPosts, List<Post> savedPosts, List<Post> likedPosts) {
		log.info("PostSummaryResponse 응답값 만들기");
		oldestPostId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		postSummaryList = latestPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts));
	}

	public PostSummaryResponse(Slice<Post> latestPosts) {
		log.info("PostSummaryResponse 응답값 만들기 - 익명 사용자");
		oldestPostId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		postSummaryList = latestPosts.map(HomePostResponse::recentPostForAnonymous);
	}
}
