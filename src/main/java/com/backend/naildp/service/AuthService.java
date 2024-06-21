package com.backend.naildp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.RequestLoginDto;
import com.backend.naildp.dto.SuccessResponseDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	public ResponseEntity<?> signupUser(RequestLoginDto requestLoginDto) {
		User user = new User(requestLoginDto, UserRole.USER);

		userRepository.save(user);

		SuccessResponseDto successResponseDto = new SuccessResponseDto();
		successResponseDto.setSuccess(true);
		successResponseDto.setMessage("회원가입 성공");
		return ResponseEntity.ok().body(successResponseDto);

	}
}
