package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backend.naildp.dto.KakaoUserInfoDto;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.service.AuthService;
import com.backend.naildp.service.KakaoService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final KakaoService kakaoService;

	@ResponseBody
	@PostMapping("/auth/signup")
	public ResponseEntity<?> signupUser(@RequestBody LoginRequestDto loginRequestDto) {
		return authService.signupUser(loginRequestDto);
	}

	@ResponseBody
	@GetMapping("/protected")
	public String protectedEndpoint() {
		return "This is a protected endpoint";
	}
	// @GetMapping("/auth/login")
	// public ResponseEntity<?> signupUser(@RequestBody LoginRequestDto loginRequestDto) {
	// 	return authService.signupUser(loginRequestDto);
	// }

	@GetMapping("/auth/kakao/callback")
	public String kakaoLogin(@RequestParam("code") String code, HttpServletResponse response,
		RedirectAttributes redirectAttributes) throws
		JsonProcessingException {
		KakaoUserInfoDto kakaoUserInfoDto = kakaoService.kakaoLogin(code, response);
		redirectAttributes.addFlashAttribute("id", kakaoUserInfoDto.getId());
		redirectAttributes.addFlashAttribute("email", kakaoUserInfoDto.getEmail());

		return "redirect:http://localhost:3000/sign-up";
	}

	@ResponseBody
	@PostMapping("/auth/nickname")
	public ResponseEntity<?> duplicateNickname(@RequestPart("nickname") String nickname) {
		return authService.duplicateNickname(nickname);
	}

	@ResponseBody
	@PostMapping("/auth/phone")
	public ResponseEntity<?> duplicatePhone(@RequestPart("phone_number") String phoneNumber) {
		return authService.duplicatePhone(phoneNumber);
	}
}
