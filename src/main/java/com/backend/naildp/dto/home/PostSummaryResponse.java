package com.backend.naildp.dto.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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

	private Long cursorId;
	private Slice<HomePostResponse> postSummaryList;

	public PostSummaryResponse(Slice<Post> latestPosts, List<?> savedPosts, List<?> likedPosts) {
		log.info("PostSummaryResponse 응답값 만들기");
		if (!latestPosts.isEmpty()) {
			cursorId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		} else {
			cursorId = -1L;
		}
		postSummaryList = latestPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts));
	}

	public PostSummaryResponse(Slice<Post> latestPosts) {
		log.info("PostSummaryResponse 응답값 만들기 - 익명 사용자");
		cursorId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		postSummaryList = latestPosts.map(HomePostResponse::recentPostForAnonymous);
	}

	public PostSummaryResponse(Slice<Post> likedPosts, List<Post> savedPosts) {
		log.info("PostSummaryResponse 좋아요체크한 게시물 응답 만들기");
		List<Post> posts = likedPosts.getContent();
		if (posts.isEmpty()) {
			cursorId = null;
		} else {
			cursorId = posts.get(posts.size() - 1).getId();
		}
		postSummaryList = likedPosts.map(post -> HomePostResponse.likedPostResponse(post, savedPosts));
	}

	public static PostSummaryResponse createEmptyResponse() {
		log.info("게시물이 없기 때문에 빈 응답 리턴");
		return new PostSummaryResponse(-1L, new SliceImpl<>(new ArrayList<>()));
	}

	public static PostSummaryResponse createForAnonymous(Slice<Post> latestPosts) {
		log.info("PostSummaryResponse static 응답값 만들기 - 익명 사용자");
		Long oldestPostId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		return new PostSummaryResponse(oldestPostId, latestPosts.map(HomePostResponse::recentPostForAnonymous));
	}

	public static PostSummaryResponse createLikedPostSummary(Slice<Post> likedPostSlice, List<?> savedPosts) {
		log.info("PostSummaryResponse 좋아요 체크한 게시물 응답 만들기 - 정적 팩토리 메서드");
		Long oldestPostLikeId = likedPostSlice.getContent().get(likedPostSlice.getNumberOfElements() - 1).getId();
		log.info("PostSummaryResponse 응답");
		return new PostSummaryResponse(oldestPostLikeId,
			likedPostSlice.map(post -> HomePostResponse.likedPostResponse(post, savedPosts)));
	}
}
