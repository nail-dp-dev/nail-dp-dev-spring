package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.dto.NicknameRequestDto;
import com.backend.naildp.dto.PhoneNumberRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.AuthService;
import com.backend.naildp.service.KakaoService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final KakaoService kakaoService;

	@PostMapping("/auth/signup")
	public ResponseEntity<ApiResponse<?>> signupUser(@RequestBody LoginRequestDto loginRequestDto,
		HttpServletRequest req,
		HttpServletResponse res) {
		return authService.signupUser(loginRequestDto, req, res);
	}

	@GetMapping("/protected")
	public String protectedEndpoint() {
		return "This is a protected endpoint";
	}

	@GetMapping("/auth/kakao")
	public ResponseEntity<ApiResponse<?>> kakaoLogin(@RequestParam("code") String code, HttpServletResponse res) throws
		JsonProcessingException {
		return kakaoService.kakaoLogin(code, res);
	}

	@PostMapping("/auth/nickname")
	public ResponseEntity<ApiResponse<?>> duplicateNickname(@RequestBody NicknameRequestDto requestDto) {
		return authService.duplicateNickname(requestDto);
	}

	@PostMapping("/auth/phone")
	public ResponseEntity<ApiResponse<?>> duplicatePhone(@RequestBody PhoneNumberRequestDto requestDto) {
		return authService.duplicatePhone(requestDto);
	}
}
