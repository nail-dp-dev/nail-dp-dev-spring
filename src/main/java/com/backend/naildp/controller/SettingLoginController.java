package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.exception.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/setting/")
public class SettingLoginController {

	@PatchMapping("connection")
	ResponseEntity<ApiResponse<?>> connectionSocialLogin(RequestBody)
}
