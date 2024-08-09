package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.ArchiveRequestDto;
import com.backend.naildp.dto.archive.ArchiveResponseDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.ArchiveService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ArchiveController {

	private final ArchiveService archiveService;

	@PostMapping("/archive")
	ResponseEntity<ApiResponse<?>> createArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ArchiveRequestDto archiveRequestDto) {

		archiveService.createArchive(userDetails.getUser().getNickname(), archiveRequestDto);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "새 아카이브 생성 성공", 2001));
	}

	@GetMapping("/archive")
	ResponseEntity<ApiResponse<?>> getArchives(@AuthenticationPrincipal UserDetailsImpl userDetails) {

		ArchiveResponseDto response = archiveService.getArchives(userDetails.getUser().getNickname());

		return ResponseEntity.ok(ApiResponse.successResponse(response, "아카이브 조회 성공", 2000));
	}

	@PostMapping("/archive/{postId}")
	ResponseEntity<ApiResponse<?>> saveArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("postId") Long postId, @RequestBody ArchiveIdRequestDto requestDto) {

		archiveService.saveArchive(userDetails.getUser().getNickname(), postId, requestDto);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "게시물 저장 성공", 2001));
	}
}