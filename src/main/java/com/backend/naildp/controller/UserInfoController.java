package com.backend.naildp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.userInfo.UserInfoResponseDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.security.UserDetailsImpl;
import com.backend.naildp.service.UserInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserInfoController {

	private final UserInfoService userInfoService;

	@GetMapping()
	ResponseEntity<ApiResponse<?>> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {

		UserInfoResponseDto userInfoResponseDto = userInfoService.getUserInfo(userDetails.getUser().getNickname());

		return ResponseEntity.ok(ApiResponse.successResponse(userInfoResponseDto, "사용자 정보 조회 성공", 2000));

	}

	@GetMapping("/point")
	ResponseEntity<ApiResponse<?>> getPoint(@AuthenticationPrincipal UserDetailsImpl userDetails) {

		Map<String, Object> point = userInfoService.getPoint(userDetails.getUser().getNickname());

		return ResponseEntity.ok(ApiResponse.successResponse(point, "포인트 조회 성공", 2000));

	}

	@PostMapping("/profile")
	ResponseEntity<ApiResponse<?>> uploadProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestPart(value = "photos") MultipartFile file) {

		userInfoService.uploadProfile(userDetails.getUser().getNickname(), file);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "프로필 이미지 업로드 성공", 2001));
	}

}
