package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		return userInfoService.getUserInfo(userDetails.getUser().getNickname());

	}
}
