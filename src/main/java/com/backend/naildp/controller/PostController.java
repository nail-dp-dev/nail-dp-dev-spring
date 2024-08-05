package com.backend.naildp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.PostInfoResponse;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TempPostRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.PostService;
import com.backend.naildp.validation.ValidationSequence;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping()
	ResponseEntity<ApiResponse<?>> uploadPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@Valid @RequestPart(value = "content") PostRequestDto postRequestDto,
		@RequestPart(value = "photos", required = true) List<MultipartFile> files) {

		if (files == null) {
			throw new CustomException("Not Input File", ErrorCode.FILE_EXCEPTION);
		}
		postService.uploadPost(userDetails.getUser().getNickname(), postRequestDto, files);

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "게시글 작성이 완료되었습니다", 2001));

	}

	@PatchMapping("/{postId}")
	ResponseEntity<ApiResponse<?>> editPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@Validated(ValidationSequence.class) @RequestPart(value = "content") PostRequestDto postRequestDto,
		@RequestPart(value = "photos", required = false) List<MultipartFile> files,
		@PathVariable("postId") Long postId) {

		postService.editPost(userDetails.getUser().getNickname(), postRequestDto, files, postId);

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "게시글 수정이 완료되었습니다", 2001));

	}

	@GetMapping("/edit/{postId}")
	ResponseEntity<ApiResponse<?>> getEditingPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("postId") Long postId) {

		EditPostResponseDto editPostResponseDto = postService.getEditingPost(userDetails.getUser().getNickname(),
			postId);

		return ResponseEntity.ok().body(ApiResponse.successResponse(editPostResponseDto, "수정 게시글 조회 완료", 2000));
	}

	@PostMapping("/temp")
	ResponseEntity<ApiResponse<?>> tempSavePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestPart(value = "content", required = false) TempPostRequestDto tempPostRequestDto,
		@RequestPart(value = "photos", required = false) List<MultipartFile> files) {
		postService.tempSavePost(userDetails.getUser().getNickname(), tempPostRequestDto, files);

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "게시글 임시저장이 완료되었습니다", 2001));
	}

	@GetMapping("/{postId}")
	ResponseEntity<ApiResponse<?>> postDetails(@PathVariable("postId") Long postId,
		@AuthenticationPrincipal User user) {
		PostInfoResponse postInfoResponse = postService.postInfo(user.getUsername(), postId);
		return ResponseEntity.ok(ApiResponse.successResponse(postInfoResponse, "특정 게시물 상세정보 조회", 2000));
	}

}