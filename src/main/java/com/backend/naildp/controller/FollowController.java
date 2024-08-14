package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	@PostMapping("/{nickname}/follow")
	ResponseEntity<?> follow(@PathVariable("nickname") String nickname,
		@AuthenticationPrincipal UserDetails userDetails) {
		followService.followUser(nickname, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "팔로우 성공", 2001));
	}

	@DeleteMapping("/{nickname}/follow")
	ResponseEntity<?> unfollow(@PathVariable("nickname") String nickname,
		@AuthenticationPrincipal UserDetails userDetails) {
		followService.unfollowUser(nickname, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "팔로우 취소 성공", 2000));
	}

	@GetMapping("/{nickname}/followers")
	ResponseEntity<?> followerCount(@PathVariable("nickname") String nickname,
		@AuthenticationPrincipal UserDetails userDetails) {
		int followerCount = followService.countFollower(nickname);
		return ResponseEntity.ok(ApiResponse.successResponse(followerCount, "팔로우 수 조회", 2000));
	}
}
