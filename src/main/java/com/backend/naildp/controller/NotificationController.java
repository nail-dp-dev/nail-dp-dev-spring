package com.backend.naildp.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	ResponseEntity<?> notifications(@PageableDefault(size = 50) Pageable pageable,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return ResponseEntity.ok(
			ApiResponse.successResponse(
				notificationService.allNotifications(pageable, userDetails.getUser().getNickname()),
				"전체 알림 조회", 2000));
	}
}
