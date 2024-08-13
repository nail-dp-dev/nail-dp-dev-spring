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
import com.backend.naildp.service.CommentLikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentLikeController {

	private final CommentLikeService commentLikeService;

	@PostMapping("/{postId}/comment/{commentId}/like")
	ResponseEntity<?> likeComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		Long commentLikeId = commentLikeService.likeComment(postId, commentId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(commentLikeId, "댓글 좋아요 성공", 2001));
	}

	@DeleteMapping("/{postId}/comment/{commentId}/like")
	ResponseEntity<?> cancelLike(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		commentLikeService.cancelCommentLike(postId, commentId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "댓글 좋아요 취소 성공", 2000));
	}

	@GetMapping("/{postId}/comment/{commentId}/like")
	ResponseEntity<?> commentLikeCount(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		PostLikeCountResponse likeCountResponse = commentLikeService.countCommentLikes(postId, commentId,
			userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(likeCountResponse, "댓글 좋아요 개수 조회 성공", 2000));
	}
}
