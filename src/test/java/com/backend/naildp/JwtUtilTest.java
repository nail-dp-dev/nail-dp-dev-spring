package com.backend.naildp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.oauth2.jwt.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Disabled
@SpringBootTest
public class JwtUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtilTest.class);

	@InjectMocks
	private JwtUtil jwtUtil;

	@BeforeEach
	public void setUp() {
		// Mockito 초기화
		MockitoAnnotations.openMocks(this);
		// secretKey 값을 수동으로 설정
		ReflectionTestUtils.setField(jwtUtil, "secretKey",
			"d42ad6111848f136a3de63282954ec1fb581f7cc9c8cc3a6e63fd34547ec7a29d2362ca9f9ba99af7bb7c56fa5af71fd862ad6c44591172357fe0b52448cfa29");
		jwtUtil.init();
		logger.info("setup completed");

	}

	@Test
	@DisplayName("JWT 토큰 생성 테스트")
	public void testCreateToken() {
		String token = jwtUtil.createToken("testuser", UserRole.USER);
		assertNotNull(token);
		assertTrue(token.startsWith(JwtUtil.BEARER_PREFIX));
		logger.info(token);

	}

	@Test
	@DisplayName("JWT 토큰 검증 테스트")
	public void testValidateToken() {
		String token = jwtUtil.createToken("testuser", UserRole.USER);
		token = jwtUtil.substringToken(token);
		assertTrue(jwtUtil.validateToken(token));
		logger.info(token);

	}

	@Test
	@DisplayName("JWT 토큰에서 사용자 정보 추출 테스트")
	public void testGetUserInfoFromToken() {
		String token = jwtUtil.createToken("testuser", UserRole.USER);
		token = jwtUtil.substringToken(token);
		Claims claims = jwtUtil.getUserInfoFromToken(token);
		assertEquals("testuser", claims.getSubject());
		assertEquals(UserRole.USER.name(), claims.get(JwtUtil.AUTHORIZATION_KEY));
		logger.info(claims.getSubject());
		logger.info((String)claims.get(JwtUtil.AUTHORIZATION_KEY));
	}

	@Test
	@DisplayName("HTTP 요청에서 JWT 토큰 추출 테스트")
	public void testGetTokenFromRequest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken("testuser", UserRole.USER));
		when(request.getCookies()).thenReturn(new Cookie[] {cookie});

		String token = jwtUtil.getTokenFromRequest(request);
		assertNotNull(token);
		logger.info(token);

	}
}