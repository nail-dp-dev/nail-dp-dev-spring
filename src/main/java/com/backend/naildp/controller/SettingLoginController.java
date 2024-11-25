package com.backend.naildp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/setting/")
public class SettingLoginController {

	// private final SettingLoginService settingLoginService;
	//
	// @PatchMapping("connection")
	// ResponseEntity<ApiResponse<?>> connectionSocialLogin(@RequestParam("type") ProviderType type,
	// 	@AuthenticationPrincipal
	// 	UserDetailsImpl userDetails) {
	// 	settingLoginService.connectionSocialLogin(userDetails.getUser().getNickname(), type);
	// }
}
