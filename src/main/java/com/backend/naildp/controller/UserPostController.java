package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.UserPostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserPostController {

	private final UserPostService userPostService;

	@GetMapping("/{nickname}/posts")
	ResponseEntity<ApiResponse<?>> getUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("nickname") String nickname,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "oldestPostId") long cursorPostId) {

		PostSummaryResponse postSummaryResponse = userPostService.getUserPosts(userDetails.getUser().getNickname(),
			nickname, size, cursorPostId);

		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "유저 게시물 조회 성공", 2000));

	}
}
