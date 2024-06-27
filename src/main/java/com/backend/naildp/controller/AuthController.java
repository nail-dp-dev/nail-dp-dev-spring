package com.backend.naildp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.KakaoUserInfoDto;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.dto.UserInfoResponseDto;
import com.backend.naildp.service.AuthService;
import com.backend.naildp.service.KakaoService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final KakaoService kakaoService;

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

	@GetMapping("/auth/kakao/callback")
	public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws
		JsonProcessingException {

		// HttpHeaders headers = new HttpHeaders();
		// headers.setLocation(URI.create("http://localhost:3000/sign-up"));

		KakaoUserInfoDto kakaoUserInfoDto = kakaoService.kakaoLogin(code, response);
		UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto();

		userInfoResponseDto.setSuccess(true);
		userInfoResponseDto.setKakaoUserInfoDto(kakaoUserInfoDto);
		userInfoResponseDto.setMessage("회원가입 완료");

		return new ResponseEntity<>(userInfoResponseDto, HttpStatus.OK);
	}

	@PostMapping("/auth/nickname")
	public ResponseEntity<?> duplicateNickname(@RequestPart("nickname") String nickname) {
		return authService.duplicateNickname(nickname);
	}

	@PostMapping("/auth/phone")
	public ResponseEntity<?> duplicatePhone(@RequestPart("phone_number") String phoneNumber) {
		return authService.duplicatePhone(phoneNumber);
	}
}
