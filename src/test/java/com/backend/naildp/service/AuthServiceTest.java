package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.backend.naildp.JwtUtilTest;
import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;
import com.backend.naildp.dto.auth.NicknameRequestDto;
import com.backend.naildp.dto.auth.PhoneNumberRequestDto;
import com.backend.naildp.dto.auth.SocialUserInfoDto;
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

@ExtendWith(MockitoExtension.class) //@Mock 사용
class AuthServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtilTest.class);

	@Mock
	UserRepository userRepository;
	@Mock
	CookieUtil cookieUtil;
	@Mock
	SocialLoginRepository socialLoginRepository;
	@Mock
	ProfileRepository profileRepository;
	@Mock
	JwtUtil jwtUtil;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse res;

	@Mock
	JwtAuthorizationFilter jwtAuthorizationFilter;

	@InjectMocks
	private AuthService authService;
	@Mock
	private UsersProfileRepository usersProfileRepository;

	private LoginRequestDto loginRequestDto;
	private SocialUserInfoDto kakaoUserInfoDto;

	@BeforeEach
	public void setUp() {
		loginRequestDto = new LoginRequestDto();
		loginRequestDto.setNickname("alswl123");
		loginRequestDto.setPhoneNumber("010-1234-5678");

		kakaoUserInfoDto = new SocialUserInfoDto(123L, "alswl123@naver.com", "http://naver.com/profile.jpg");
	}

	@Test
	@DisplayName("회원가입 성공")
	public void testSignup_Success() {
		// given
		given(userRepository.findByNickname(loginRequestDto.getNickname())).willReturn(Optional.empty());
		given(cookieUtil.getUserInfoFromCookie(req)).willReturn(kakaoUserInfoDto);
		doNothing().when(cookieUtil).deleteCookie("userInfo", req, res);
		given(jwtUtil.createToken(anyString(), any(UserRole.class))).willReturn("Authorization");

		// when
		ResponseEntity<ApiResponse<?>> response = authService.signupUser(loginRequestDto, req, res);

		// then
		assertEquals(2001, response.getBody().getCode());
		assertEquals("회원가입 완료되었습니다", response.getBody().getMessage());
		verify(userRepository).save(any(User.class));
		verify(socialLoginRepository).save(any(SocialLogin.class));
		verify(profileRepository).save(any(Profile.class));
		verify(usersProfileRepository).save(any(UsersProfile.class));
		verify(cookieUtil).deleteCookie("userInfo", req, res);
		verify(jwtUtil).addJwtToCookie("Authorization", res);
	}

	@Test
	@DisplayName("회원가입 실패")
	public void testSignup_fail() {
		// given
		User existingUser = createUser();
		given(userRepository.findByNickname(loginRequestDto.getNickname())).willReturn(Optional.of(existingUser));

		// when
		CustomException exception = assertThrows(CustomException.class, () -> {
			authService.signupUser(loginRequestDto, req, res);
		});
		// then
		assertEquals("이미 존재하는 사용자입니다.", exception.getMessage());
		assertEquals(ErrorCode.ALREADY_EXIST, exception.getErrorCode());
		verify(userRepository, never()).save(any(User.class));
		verify(socialLoginRepository, never()).save(any(SocialLogin.class));
		verify(profileRepository, never()).save(any(Profile.class));
		verify(cookieUtil, never()).deleteCookie(anyString(), any(HttpServletRequest.class),
			any(HttpServletResponse.class));
		verify(jwtUtil, never()).addJwtToCookie(anyString(), any(HttpServletResponse.class));
	}

	private User createUser() {
		return User.builder()
			.nickname("alswl123")
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.agreement(true)
			.build();
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 사용 가능")
	public void testDuplicateNickname_Success() {
		// given
		NicknameRequestDto requestDto = new NicknameRequestDto();
		requestDto.setNickname("newNickname");
		given(userRepository.findByNickname(requestDto.getNickname())).willReturn(Optional.empty());

		// when
		ResponseEntity<ApiResponse<?>> response = authService.duplicateNickname(requestDto);

		// then
		assertEquals(2000, response.getBody().getCode());
		assertEquals("사용 가능한 닉네임입니다", response.getBody().getMessage());
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 이미 존재")
	public void testDuplicateNickname_fail() {
		// given
		NicknameRequestDto requestDto = new NicknameRequestDto();
		requestDto.setNickname("alswl123");
		User user = createUser();
		given(userRepository.findByNickname(requestDto.getNickname())).willReturn(Optional.of(user));

		// when
		CustomException exception = assertThrows(CustomException.class, () -> {
			authService.duplicateNickname(requestDto);
		});

		// then
		assertEquals("이미 존재하는 닉네임입니다", exception.getMessage());
		assertEquals(ErrorCode.ALREADY_EXIST, exception.getErrorCode());
	}

	@Test
	@DisplayName("전화번호 중복 확인 - 사용 가능")
	public void testDuplicatePhone_Success() {
		// given
		PhoneNumberRequestDto requestDto = new PhoneNumberRequestDto();
		requestDto.setPhoneNumber("010-1234-5678");
		given(userRepository.findByPhoneNumber(requestDto.getPhoneNumber())).willReturn(Optional.empty());

		// when
		ResponseEntity<ApiResponse<?>> response = authService.duplicatePhone(requestDto);

		// then
		assertEquals(2000, response.getBody().getCode());
		assertEquals("사용 가능한 전화번호입니다", response.getBody().getMessage());
	}

	@Test
	@DisplayName("전화번호 중복 확인 - 이미 존재")
	public void testDuplicatePhone_fail() {
		// given
		PhoneNumberRequestDto requestDto = new PhoneNumberRequestDto();
		requestDto.setPhoneNumber("010-1234-5678");
		User user = createUser();
		given(userRepository.findByPhoneNumber(requestDto.getPhoneNumber())).willReturn(Optional.of(user));

		// when
		CustomException exception = assertThrows(CustomException.class, () -> {
			authService.duplicatePhone(requestDto);
		});
		logger.info(String.valueOf(exception));

		// then
		assertEquals("이미 존재하는 전화번호입니다", exception.getMessage());
		assertEquals(ErrorCode.ALREADY_EXIST, exception.getErrorCode());
	}

	@Test
	@DisplayName("쿠키 검증 성공")
	public void testCheckCookie_Success() {
		// given
		String tokenValue = "validToken";
		Claims claims = mock(Claims.class);
		given(jwtUtil.getTokenFromRequest(req)).willReturn(tokenValue);
		given(jwtUtil.substringToken(tokenValue)).willReturn(tokenValue);
		given(jwtUtil.validateToken(tokenValue)).willReturn(true);
		given(jwtUtil.getUserInfoFromToken(tokenValue)).willReturn(claims);
		given(claims.getSubject()).willReturn("testUser");

		// when
		ResponseEntity<ApiResponse<?>> response = authService.checkCookie(req);

		// then
		assertEquals(2000, response.getBody().getCode());
		assertEquals("jwt토큰 검증 확인", response.getBody().getMessage());
	}

	@Test
	@DisplayName("잘못된 토큰 예외")
	public void testCheckCookie_InvalidToken() {
		// given
		String tokenValue = "invalidToken";
		given(jwtUtil.getTokenFromRequest(req)).willReturn(tokenValue);
		given(jwtUtil.substringToken(tokenValue)).willReturn(tokenValue);
		given(jwtUtil.validateToken(tokenValue)).willThrow(new RuntimeException("Authentication 에러"));

		// when
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.checkCookie(req);
		});

		// then
		assertEquals("Authentication 에러", exception.getMessage());
	}

	@Test
	@DisplayName("JWT 토큰이 존재하지 않을 때")
	public void testCheckCookie_NoToken() {
		// given
		given(jwtUtil.getTokenFromRequest(req)).willReturn(null);

		// when
		NullPointerException exception = assertThrows(NullPointerException.class, () -> {
			authService.checkCookie(req);
		});

		// then
		assertEquals("jwt 토큰이 존재하지 않습니다.", exception.getMessage());
	}
}
