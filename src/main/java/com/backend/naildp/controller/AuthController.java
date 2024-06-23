package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.service.AuthService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	//https://kauth.kakao.com/oauth/authorize?client_id={REST_API_KEY}&redirect_uri={REDIRECT_URI}&response_type=code
	@PostMapping("/auth/signup")
	public ResponseEntity<?> signupUser(@RequestBody LoginRequestDto loginRequestDto) {
		return authService.signupUser(loginRequestDto);
	}

	@GetMapping("/protected")
	public String protectedEndpoint() {
		return "This is a protected endpoint";
	}
	// @GetMapping("/auth/login")
	// public ResponseEntity<?> signupUser(@RequestBody LoginRequestDto loginRequestDto) {
	// 	return authService.signupUser(loginRequestDto);
	// }
}
