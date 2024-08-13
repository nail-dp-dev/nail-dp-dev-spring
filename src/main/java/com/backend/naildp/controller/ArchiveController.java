package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.ArchiveResponseDto;
import com.backend.naildp.dto.archive.CreateArchiveRequestDto;
import com.backend.naildp.dto.archive.PostIdRequestDto;
import com.backend.naildp.dto.home.PostSummaryResponse;
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
		@RequestBody CreateArchiveRequestDto requestDto) {

		archiveService.createArchive(userDetails.getUser().getNickname(), requestDto);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "새 아카이브 생성 성공", 2001));
	}

	@GetMapping("/archive")
	ResponseEntity<ApiResponse<?>> getArchives(@AuthenticationPrincipal UserDetailsImpl userDetails) {

		ArchiveResponseDto response = archiveService.getArchives(userDetails.getUser().getNickname());

		return ResponseEntity.ok(ApiResponse.successResponse(response, "아카이브 조회 성공", 2000));
	}

	@PostMapping("/archive/{archiveId}")
	ResponseEntity<ApiResponse<?>> saveArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("archiveId") Long archiveId, @RequestBody PostIdRequestDto requestDto) {

		archiveService.saveArchive(userDetails.getUser().getNickname(), archiveId, requestDto.getPostId());

		return ResponseEntity.ok(ApiResponse.successResponse(null, "아카이브에 게시물 저장 성공", 2001));
	}

	@PostMapping("/archive/copy")
	ResponseEntity<ApiResponse<?>> copyArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ArchiveIdRequestDto requestDto) {

		archiveService.copyArchive(userDetails.getUser().getNickname(), requestDto);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "아카이브 복제 성공", 2001));
	}

	@GetMapping("/archive/follow")
	ResponseEntity<ApiResponse<?>> getFollowingArchives(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "cursorId") long cursorId) {

		PostSummaryResponse postSummaryResponse = archiveService.getFollowingArchives(
			userDetails.getUser().getNickname(), size, cursorId);

		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "팔로잉 아카이브 리스트 조회 성공", 2000));
	}

	@DeleteMapping("/archive")
	ResponseEntity<ApiResponse<?>> deleteArchive(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ArchiveIdRequestDto requestDto) {

		archiveService.deleteArchive(userDetails.getUser().getNickname(), requestDto.getArchiveId());

		return ResponseEntity.ok(ApiResponse.successResponse(null, "아카이브 삭제 성공", 2001));
	}

	@GetMapping("/archive/{archiveId}")
	ResponseEntity<ApiResponse<?>> getArchivePosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("archiveId") Long archiveId,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "cursorId") long cursorId) {

		PostSummaryResponse postSummaryResponse = archiveService.getArchivePosts(
			userDetails.getUser().getNickname(), archiveId, size, cursorId);

		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "특정 아카이브 내 게시물 조회 성공", 2000));
	}

	@GetMapping("/archive/{archiveId}/like")
	ResponseEntity<ApiResponse<?>> getLikedArchivePosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable("archiveId") Long archiveId,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, defaultValue = "-1", value = "cursorId") long cursorId) {

		PostSummaryResponse postSummaryResponse = archiveService.getLikedArchivePosts(
			userDetails.getUser().getNickname(), archiveId, size, cursorId);

		return ResponseEntity.ok(ApiResponse.successResponse(postSummaryResponse, "특정 아카이브 내 게시물 좋아요 조회 성공", 2000));
	}
}