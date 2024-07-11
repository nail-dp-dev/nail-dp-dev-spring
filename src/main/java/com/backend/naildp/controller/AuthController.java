package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.auth.NicknameRequestDto;
import com.backend.naildp.dto.auth.PhoneNumberRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.AuthService;
import com.backend.naildp.service.KakaoService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final KakaoService kakaoService;

	@PostMapping("/auth/signup")
	public ResponseEntity<ApiResponse<?>> signupUser(@Valid @RequestBody LoginRequestDto loginRequestDto,
		HttpServletRequest req,
		HttpServletResponse res) {
		return authService.signupUser(loginRequestDto, req, res);
	}

	@GetMapping("/protected")
	public String protectedEndpoint() {
		return "This is a protected endpoint";
	}

	@GetMapping("/auth/kakao")
	public ResponseEntity<ApiResponse<?>> kakaoLogin(@RequestParam("code") String code, HttpServletRequest req,
		HttpServletResponse res) throws
		JsonProcessingException {
		return kakaoService.kakaoLogin(code, req, res);
	}

	@PostMapping("/auth/nickname")
	public ResponseEntity<ApiResponse<?>> duplicateNickname(@Valid @RequestBody NicknameRequestDto requestDto) {
		return authService.duplicateNickname(requestDto);
	}

	@PostMapping("/auth/phone")
	public ResponseEntity<ApiResponse<?>> duplicatePhone(@RequestBody PhoneNumberRequestDto requestDto) {
		return authService.duplicatePhone(requestDto);
	}

	@GetMapping("/auth/cookie")
	public ResponseEntity<ApiResponse<?>> checkCookie(HttpServletRequest req) throws Exception {
		return authService.checkCookie(req);
	}

	@GetMapping("/auth/logout")
	public ResponseEntity<ApiResponse<?>> logoutUser(HttpServletRequest req, HttpServletResponse res) {
		return authService.logoutUser(req, res);
	}
}
