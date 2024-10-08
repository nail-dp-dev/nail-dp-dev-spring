package com.backend.naildp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.backend.naildp.JwtUtilTest;
import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.common.ProviderType;
import com.backend.naildp.dto.auth.SocialUserInfoDto;
import com.google.gson.Gson;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class CookieUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtilTest.class);

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse res;

	@InjectMocks
	private CookieUtil cookieUtil;

	private SocialUserInfoDto userInfo;
	private Gson gson;

	@BeforeEach
	public void setUp() {
		userInfo = new SocialUserInfoDto("123L", "alswl123@naver.com",
			"http://naver.com/profile.jpg", ProviderType.kakao);
		gson = new Gson();
	}

	@Test
	@DisplayName("userInfo 쿠키 저장")
	public void testSetUserInfoCookie() throws UnsupportedEncodingException {
		// given
		String userInfoJson = gson.toJson(userInfo);
		String encodedUserInfoJson = URLEncoder.encode(userInfoJson, "UTF-8");

		// when
		cookieUtil.setUserInfoCookie(res, userInfo);

		// then
		verify(res).addCookie(argThat(cookie ->
			"userInfo".equals(cookie.getName()) && encodedUserInfoJson.equals(cookie.getValue())
				&& cookie.getMaxAge() == 1800
		));
	}

	@Test
	@DisplayName("userInfo 쿠키 가져오기")
	public void testGetUserInfoFromCookie() throws UnsupportedEncodingException {
		// given
		String userInfoJson = gson.toJson(userInfo);
		String encodedUserInfo = URLEncoder.encode(userInfoJson, "UTF-8");
		Cookie cookie = new Cookie("userInfo", encodedUserInfo);
		given(req.getCookies()).willReturn(new Cookie[] {cookie});

		// when
		SocialUserInfoDto result = cookieUtil.getUserInfoFromCookie(req);

		// then
		assertNotNull(result);
		assertEquals(userInfo.getId(), result.getId());
		assertEquals(userInfo.getEmail(), result.getEmail());
		assertEquals(userInfo.getProfileUrl(), result.getProfileUrl());
	}

	@Test
	@DisplayName("쿠키 삭제 성공")
	public void testDeleteCookie() {
		// given
		Cookie cookie = new Cookie("userInfo", "value");
		given(req.getCookies()).willReturn(new Cookie[] {cookie});

		// when
		cookieUtil.deleteCookie("userInfo", req, res);

		// then
		verify(res).addCookie(argThat(argument ->
			"userInfo".equals(argument.getName()) && argument.getMaxAge() == 0
		));
	}

}