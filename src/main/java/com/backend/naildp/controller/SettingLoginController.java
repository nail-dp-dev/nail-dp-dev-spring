package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.common.ProviderType;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.SettingLoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/setting/")
public class SettingLoginController {

	private final SettingLoginService settingLoginService;

	@PatchMapping("connection")
	ResponseEntity<ApiResponse<?>> connectionSocialLogin(@RequestParam("type")ProviderType type, @AuthenticationPrincipal
		UserDetailsImpl userDetails){
		settingLoginService.connectionSocialLogin(userDetails.getUser().getNickname(), type);
	}
}
중