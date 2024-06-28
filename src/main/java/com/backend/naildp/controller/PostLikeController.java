package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.common.ApiResponse;
import com.backend.naildp.service.PostLikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostLikeController {

	private final PostLikeService postLikeService;

	@PostMapping("/{postId}/likes")
	public ResponseEntity<?> likePost(@PathVariable("postId") Long postId,
		@AuthenticationPrincipal UserDetails userDetails) {
		Long likePostId = postLikeService.likeByPostId(postId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(likePostId));
	}

	@DeleteMapping("/{postId}/likes")
	public ResponseEntity<?> unlikePost(@PathVariable("postId") Long postId) {
		postLikeService.unlikeByPostId(postId);
		return ResponseEntity.ok(ApiResponse.successResponse(null));
	}
}
