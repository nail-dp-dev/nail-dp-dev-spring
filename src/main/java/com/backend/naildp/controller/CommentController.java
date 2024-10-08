package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/{postId}/comment")
	ResponseEntity<?> createComment(@PathVariable("postId") Long postId,
		@Valid @RequestBody CommentRegisterDto commentRegisterDto,
		@AuthenticationPrincipal UserDetails userDetails) {
		Long registeredCommentId = commentService.registerComment(postId, commentRegisterDto,
			userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(registeredCommentId, "댓글 등록 성공", 2001));
	}

	@PatchMapping("/{postId}/comment/{commentId}")
	ResponseEntity<?> modifyComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@Valid @RequestBody CommentRegisterDto commentModifyDto,
		@AuthenticationPrincipal UserDetails userDetails) {
		Long modifiedCommentId = commentService.modifyComment(postId, commentId, commentModifyDto,
			userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(modifiedCommentId, "댓글 수정 성공", 2001));
	}

	@DeleteMapping("/{postId}/comment/{commentId}")
	ResponseEntity<?> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
		@AuthenticationPrincipal UserDetails userDetails) {
		commentService.deleteComment(postId, commentId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "댓글 삭제 성공", 2001));
	}

	@GetMapping("/{postId}/comment")
	ResponseEntity<?> findComments(@PathVariable("postId") Long postId,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "cursorId") long cursorId,
		@AuthenticationPrincipal UserDetails userDetails) {
		CommentSummaryResponse response = commentService.findComments(postId, size, cursorId,
			userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(response, "댓글 조회 성공", 2000));
	}
}
