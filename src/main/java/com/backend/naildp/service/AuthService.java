package com.backend.naildp.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.KakaoUserInfoDto;
import com.backend.naildp.dto.LoginRequestDto;
import com.backend.naildp.dto.NicknameRequestDto;
import com.backend.naildp.dto.PhoneNumberRequestDto;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
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
	public ResponseEntity<ApiResponse<?>> signupUser(LoginRequestDto loginRequestDto, HttpServletRequest req,
		HttpServletResponse res) {
		Optional<User> findUser = userRepository.findByNickname(loginRequestDto.getNickname());
		if (findUser.isPresent()) {
			throw new CustomException("이미 존재하는 사용자입니다.", ErrorCode.ALREADY_EXIST);
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

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "회원가입 완료되었습니다", 2001));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<?>> duplicateNickname(NicknameRequestDto requestDto) {
		Optional<User> user = Optional.ofNullable(userRepository.findByNickname(requestDto.getNickname()).orElseThrow(()
			-> new CustomException("잘못된 요청입니다.", ErrorCode.TEMPORARY_SERVER_ERROR)
		));
		if (user.isPresent()) {
			throw new CustomException("이미 존재하는 닉네임입니다", ErrorCode.ALREADY_EXIST);
		}
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 닉네임입니다", 2000));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<?>> duplicatePhone(PhoneNumberRequestDto requestDto) {
		Optional<User> user = Optional.ofNullable(
			userRepository.findByNickname(requestDto.getPhoneNumber()).orElseThrow(()
				-> new CustomException("잘못된 요청입니다.", ErrorCode.TEMPORARY_SERVER_ERROR)
			));
		if (user.isPresent()) {
			throw new CustomException("이미 존재하는 전화번호입니다", ErrorCode.ALREADY_EXIST);
		}
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 전화번호입니다", 2000));
	}
}
