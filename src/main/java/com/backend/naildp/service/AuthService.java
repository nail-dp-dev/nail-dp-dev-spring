package com.backend.naildp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.dto.SuccessResponseDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	public ResponseEntity<?> signupUser(LoginRequestDto loginRequestDto) {
		User user = new User(loginRequestDto, UserRole.USER);

		userRepository.save(user);

		SuccessResponseDto successResponseDto = new SuccessResponseDto();
		successResponseDto.setSuccess(true);
		successResponseDto.setMessage("회원가입 성공");
		return ResponseEntity.ok().body(successResponseDto);

	}
}
