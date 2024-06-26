package com.backend.naildp.service;

import java.util.Optional;

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

	public ResponseEntity<?> duplicateNickname(String nickname) {
		Optional<User> user = userRepository.findByNickname(nickname);
		if (user.isPresent()) {
			throw new IllegalArgumentException("Already Exist Nickname");
		}
		SuccessResponseDto successResponseDto = new SuccessResponseDto();
		successResponseDto.setSuccess(true);
		successResponseDto.setMessage("닉네임 중복 확인 성공");
		return ResponseEntity.ok().body(successResponseDto);
	}

	public ResponseEntity<?> duplicatePhone(String phoneNumber) {
		Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
		if (user.isPresent()) {
			throw new IllegalArgumentException("Already Exist PhoneNumber");
		}
		SuccessResponseDto successResponseDto = new SuccessResponseDto();
		successResponseDto.setSuccess(true);
		successResponseDto.setMessage("전화번호 중복 확인 성공");
		return ResponseEntity.ok().body(successResponseDto);
	}
}
