package com.backend.naildp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.dto.setting.NotificationSettingResponseDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.UserNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class UserSettingController {

	private final UserNotificationService userNotificationService;

	/**
	 * 알림 설정 전체 조회
	 */
	@GetMapping("/notifications")
	ResponseEntity<?> notificationSetting(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		List<NotificationSettingResponseDto> notificationSettings = userNotificationService.getNotificationSetting(
			userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(notificationSettings, "전체 알림 설정 조회", 2000));
	}

	/**
	 * 알림 설정 단건 변경
	 */
	@PatchMapping("/notifications/{type}")
	ResponseEntity<?> turnOffNotification(@PathVariable("type") NotificationType notificationType,
		@RequestParam(value = "status", defaultValue = "true") Boolean status,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		userNotificationService.changeNotificationSetting(notificationType, status, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "알림 설정 변경 완료", 2001));
	}

	/**
	 * 알림 설정 한번에 다 변경
	 */
	@PatchMapping("/notifications")
	ResponseEntity<?> turnOffAllNotification(@RequestParam(value = "status", defaultValue = "true") Boolean status,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		userNotificationService.changeAllNotificationType(status, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "알림 끄기 완료", 2001));
	}

}
