package com.backend.naildp.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.ApiResponse;
import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.KakaoUserInfoDto;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.jwt.JwtUtil;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final CookieUtil cookieUtil;
	private final SocialLoginRepository socialLoginRepository;
	private final ProfileRepository profileRepository;
	private final JwtUtil jwtUtil;

	@Transactional
	public ApiResponse<?> signupUser(LoginRequestDto loginRequestDto, HttpServletRequest req, HttpServletResponse res) {
		Optional<User> findUser = userRepository.findByNickname(loginRequestDto.getNickname());
		if (findUser.isPresent()) {
			return ApiResponse.errorResponse("Already Exist User");
		}
		User user = new User(loginRequestDto, UserRole.USER);

		userRepository.save(user);

		KakaoUserInfoDto userInfo = cookieUtil.getUserInfoFromCookie(req);
		SocialLogin socialLogin = new SocialLogin(userInfo.getId(), userInfo.getPlatform(), userInfo.getEmail(), user);
		socialLoginRepository.save(socialLogin);

		if (userInfo.getProfileUrl() != null) {
			Profile profile = new Profile(user, userInfo.getProfileUrl(), userInfo.getProfileUrl());
			profileRepository.save(profile);
		}

		cookieUtil.deleteUserInfoCookie(res);
		log.info("쿠키 지우기");

		String createToken = jwtUtil.createToken(user.getNickname(), user.getRole());
		jwtUtil.addJwtToCookie(createToken, res);

		return ApiResponse.successWithLoginType("signUp", "회원가입 완료");
	}

	@Transactional(readOnly = true)
	public ApiResponse<?> duplicateNickname(String nickname) {
		Optional<User> user = userRepository.findByNickname(nickname);
		if (user.isPresent()) {
			throw new IllegalArgumentException("Already Exist Nickname");
		}
		return ApiResponse.successWithMessage(HttpStatus.OK, "닉네임 중복 확인 성공");
	}

	@Transactional(readOnly = true)
	public ApiResponse<?> duplicatePhone(String phoneNumber) {
		Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
		if (user.isPresent()) {
			throw new IllegalArgumentException("Already Exist PhoneNumber");
		}
		return ApiResponse.successWithMessage(HttpStatus.OK, "전화번호 중복 확인 성공");
	}
}
