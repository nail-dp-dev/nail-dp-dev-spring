package com.backend.naildp.service;

import org.springframework.stereotype.Service;

import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingLoginService {
	private final UserRepository userRepository;
	private final SocialLoginRepository socialLoginRepository;

	// public void connectionSocialLogin(String nickname, ProviderType type) {
	// 	User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 사용자를 찾을 수 없습니다.",
	// 		ErrorCode.NOT_FOUND));
	// 	SocialLogin socialLogin = new
	// }
}
