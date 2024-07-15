package com.backend.naildp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.PostService;

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
			throw new CustomException("Not Input File", ErrorCode.INPUT_NULL);
		}
		return postService.uploadPost(userDetails.getUser().getNickname(), postRequestDto, files);

	}

	@PatchMapping("/{postId}")
	ResponseEntity<ApiResponse<?>> updatePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@Valid @RequestPart(value = "content", required = false) PostRequestDto postRequestDto,
		@RequestPart(value = "photos", required = false) List<MultipartFile> files,
		@PathVariable("postId") Long postId) {

		return postService.updatePost(userDetails.getUser().getNickname(), postRequestDto, files, postId);

	}
}
