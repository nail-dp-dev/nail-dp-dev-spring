package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.auth.NicknameRequestDto;
import com.backend.naildp.dto.auth.PhoneNumberRequestDto;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.AuthService;
import com.backend.naildp.validation.ValidationSequence;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	// private final KakaoService kakaoService;

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

	// @GetMapping("/auth/oauth2/code/kakao")
	// public ResponseEntity<ApiResponse<?>> oauth2LoginCallback() {
	// 	try {
	//
	// 		return ResponseEntity.ok(ApiResponse.successResponse(null, "로그인이 완료되었습니다.", 2000));
	// 	} catch (Exception e) {
	// 		log.error("OAuth2 로그인 후 처리 중 오류 발생: ", e);
	// 		return ResponseEntity.status(500).body(ApiResponse.successResponse(null, "로그인 처리 중 오류가 발생했습니다.", 5001));
	// 	}
	// }

	@PostMapping("/auth/nickname")
	public ResponseEntity<ApiResponse<?>> duplicateNickname(
		@Validated(ValidationSequence.class) @RequestBody NicknameRequestDto requestDto) {
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
