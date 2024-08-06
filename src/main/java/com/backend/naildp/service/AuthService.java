package com.backend.naildp.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.common.ProfileType;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.KakaoUserInfoDto;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.auth.NicknameRequestDto;
import com.backend.naildp.dto.auth.PhoneNumberRequestDto;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.SocialLogin;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UsersProfile;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.jwt.JwtAuthorizationFilter;
import com.backend.naildp.jwt.JwtUtil;
import com.backend.naildp.repository.ProfileRepository;
import com.backend.naildp.repository.SocialLoginRepository;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UsersProfileRepository;

import io.jsonwebtoken.Claims;
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
	private final JwtAuthorizationFilter jwtAuthorizationFilter;
	private final UsersProfileRepository usersProfileRepository;

	@Transactional
	public ResponseEntity<ApiResponse<?>> signupUser(LoginRequestDto loginRequestDto, HttpServletRequest req,
		HttpServletResponse res) {
		Optional<User> findUser = userRepository.findByNickname(loginRequestDto.getNickname());
		if (findUser.isPresent()) {
			throw new CustomException("이미 존재하는 사용자입니다.", ErrorCode.ALREADY_EXIST);
		}
		User user = User.builder()
			.thumbnailUrl("default")
			.nickname(loginRequestDto.getNickname())
			.phoneNumber(loginRequestDto.getPhoneNumber())
			.agreement(loginRequestDto.isAgreement())
			.role(UserRole.USER)
			.build();

		userRepository.save(user);

		KakaoUserInfoDto userInfo = cookieUtil.getUserInfoFromCookie(req);
		SocialLogin socialLogin = new SocialLogin(userInfo.getId(), userInfo.getPlatform(), userInfo.getEmail(), user);
		socialLoginRepository.save(socialLogin);

		if (userInfo.getProfileUrl() != null) {
			Profile profile = Profile.builder()
				.profileUrl(userInfo.getProfileUrl())
				.name(userInfo.getProfileUrl())
				.thumbnail(true)
				.profileType(ProfileType.AUTO)
				.build();

			UsersProfile usersProfile = UsersProfile.builder()
				.profile(profile)
				.user(user)
				.build();

			profileRepository.save(profile);
			usersProfileRepository.save(usersProfile);
			user.thumbnailUrlUpdate(userInfo.getProfileUrl());
		}

		cookieUtil.deleteCookie("userInfo", req, res);
		log.info("쿠키 지우기");

		String createToken = jwtUtil.createToken(user.getNickname(), user.getRole());
		jwtUtil.addJwtToCookie(createToken, res);

		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "회원가입 완료되었습니다", 2001));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<?>> duplicateNickname(NicknameRequestDto requestDto) {
		Optional<User> user = userRepository.findByNickname(requestDto.getNickname());
		if (user.isPresent()) {
			throw new CustomException("이미 존재하는 닉네임입니다", ErrorCode.ALREADY_EXIST);
		}
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 닉네임입니다", 2000));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<?>> duplicatePhone(PhoneNumberRequestDto requestDto) {
		Optional<User> user = userRepository.findByPhoneNumber(requestDto.getPhoneNumber());
		if (user.isPresent()) {
			throw new CustomException("이미 존재하는 전화번호입니다", ErrorCode.ALREADY_EXIST);
		}
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "사용 가능한 전화번호입니다", 2000));
	}

	public ResponseEntity<ApiResponse<?>> checkCookie(HttpServletRequest req) {

		String tokenValue = jwtUtil.getTokenFromRequest(req);
		if (tokenValue == null) {
			throw new NullPointerException("jwt 토큰이 존재하지 않습니다.");
		}

		if (StringUtils.hasText(tokenValue)) {
			// JWT 토큰 substring
			tokenValue = jwtUtil.substringToken(tokenValue);
			jwtUtil.validateToken(tokenValue);
			Claims info = jwtUtil.getUserInfoFromToken(tokenValue);

			try {
				jwtAuthorizationFilter.setAuthentication(info.getSubject());
			} catch (Exception e) {
				throw new RuntimeException("Authentication 에러", e);
			}
		}
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "jwt토큰 검증 확인", 2000));
	}

	public ResponseEntity<ApiResponse<?>> logoutUser(HttpServletRequest req, HttpServletResponse res) {
		cookieUtil.deleteCookie(JwtUtil.AUTHORIZATION_HEADER, req, res);
		return ResponseEntity.ok().body(ApiResponse.successResponse(null, "로그아웃 성공", 2000));
	}
}