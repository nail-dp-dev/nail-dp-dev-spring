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

import com.backend.naildp.dto.postLike.PostLikeCountResponse;
import com.backend.naildp.exception.ApiResponse;
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
		return ResponseEntity.ok(ApiResponse.successResponse(null, "좋아요 목록에 추가", 2001));
	}

	@DeleteMapping("/{postId}/likes")
	public ResponseEntity<?> unlikePost(@PathVariable("postId") Long postId,
		@AuthenticationPrincipal UserDetails userDetails) {
		postLikeService.unlikeByPostId(postId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "좋아요 취소", 2001));
	}

	@GetMapping("/{postId}/likes")
	ResponseEntity<?> likeCount(@PathVariable("postId") Long postId, @AuthenticationPrincipal UserDetails userDetails) {
		PostLikeCountResponse postLikeCountResponse = postLikeService.countPostLike(postId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(postLikeCountResponse, "게시글 좋아요 숫자 조회 완료", 2000));
	}
}
