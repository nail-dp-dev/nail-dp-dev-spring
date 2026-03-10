package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TestController {

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	@GetMapping("/token")
	public ResponseEntity<String> getToken(@RequestParam String nickname) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new UsernameNotFoundException(nickname));
		String accessToken = jwtUtil.createPermanentToken(user.getNickname(), user.getRole());
		return ResponseEntity.ok(accessToken);
	}
}
