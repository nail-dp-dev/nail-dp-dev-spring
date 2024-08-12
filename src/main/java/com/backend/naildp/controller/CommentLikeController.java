package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.CommentLikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentLikeController {

	private final CommentLikeService commentLikeService;

	@PostMapping("/{postId}/comment/{commentId}")
	ResponseEntity<?> likeComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		Long commentLikeId = commentLikeService.likeComment(postId, commentId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(commentLikeId, "댓글 좋아요 성공", 2001));
	}
}
