package com.backend.naildp.controller.post;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.post.PostInfoContext;
import com.backend.naildp.service.post.PostInfoService;
import com.backend.naildp.service.post.v2.PostInfoContextV2;
import com.backend.naildp.service.post.v2.PostInfoContextV2_1;
import com.backend.naildp.service.post.v2.PostInfoContextV2_2;
import com.backend.naildp.service.post.v2.PostInfoContextV3;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

	private final PostInfoService postInfoService;
	private final PostInfoContext postInfoContext;
	private final PostInfoContextV2 postInfoContextV2;
	private final PostInfoContextV2_1 postInfoContextV2_1;
	private final PostInfoContextV2_2 postInfoContextV2_2;
	private final PostInfoContextV3 postInfoContextV3;

	@GetMapping("/home")
	public ResponseEntity<?> homePosts(
		@RequestParam(name = "choice", defaultValue = "new") String choice,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "oldestPostId") Long cursorPostId) {
		PostSummaryResponse postSummaryResponse = postInfoContext.posts(choice, size, cursorPostId);
		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회", 2000));
	}

	@GetMapping("/posts/like")
	public ResponseEntity<?> likedPost(@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "oldestPostLikeId") long postLikeId) {
		PostSummaryResponse likedPostsResponses = postInfoService.findLikedPost(userDetails.getUsername(), size,
			postLikeId);
		return ResponseEntity.ok(ApiResponse.successResponse(likedPostsResponses, "좋아요 체크한 게시물 조회", 2000));
	}

	@GetMapping("/home/v2")
	public ResponseEntity<?> homePostsV2(@RequestParam(name = "choice", defaultValue = "foryou") String choice,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "oldestPostId") Long cursorPostId) {
		PostSummaryResponse postSummaryResponse = postInfoContextV2.posts(choice, size, cursorPostId);
		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회", 2000));
	}

	@GetMapping("/home/v2_1")
	public ResponseEntity<?> homePostsV2_1(@RequestParam(name = "choice", defaultValue = "foryou") String choice,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "oldestPostId") Long cursorPostId) {
		PostSummaryResponse postSummaryResponse = postInfoContextV2_1.posts(choice, size, cursorPostId);
		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회", 2000));
	}

	@GetMapping("/home/v2_2")
	public ResponseEntity<?> homePostsV2_2(@RequestParam(name = "choice", defaultValue = "foryou") String choice,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "oldestPostId") Long cursorPostId) {
		PostSummaryResponse postSummaryResponse = postInfoContextV2_2.posts(choice, size, cursorPostId);
		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회", 2000));
	}

	@GetMapping("/home/v3")
	public ResponseEntity<?> homePostsV3(@RequestParam(name = "choice", defaultValue = "foryou") String choice,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "oldestPostId") Long cursorPostId) {
		PostSummaryResponse postSummaryResponse = postInfoContextV3.posts(choice, size, cursorPostId);
		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "게시물 조회 V3", 2000));
	}
}
