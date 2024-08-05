package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/{postId}/comment")
	ResponseEntity<?> createComment(@PathVariable("postId") Long postId,
		@RequestBody CommentRegisterDto commentRegisterDto,
		@AuthenticationPrincipal User user) {
		Long registeredCommentId = commentService.registerComment(postId, commentRegisterDto, user.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(registeredCommentId, "댓글 등록 성공", 2001));
	}

	@PatchMapping("/{postId}/comment/{commentId}")
	ResponseEntity<?> modifyComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@RequestBody CommentRegisterDto commentModifyDto,
		@AuthenticationPrincipal User user) {
		Long modifiedCommentId = commentService.modifyComment(postId, commentId, commentModifyDto, user.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(modifiedCommentId, "댓글 등록 성공", 2001));
	}
}
