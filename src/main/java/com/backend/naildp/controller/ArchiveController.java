package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.archive.ArchiveRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.ArchiveService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ArchiveController {

	private final ArchiveService archiveService;

	@SuppressWarnings("checkstyle:Indentation")
	@PostMapping("/archive")
	ResponseEntity<ApiResponse<?>> createArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ArchiveRequestDto archiveRequestDto) {

		archiveService.createArchive(userDetails.getUser().getNickname(), archiveRequestDto);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "프로필 이미지 업로드 성공", 2001));
	}

}
