package com.backend.naildp.oauth2;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.backend.naildp.common.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	private static final String OAUTH2_STATE_COOKIE_NAME = "oauth2_state";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtil.getStateCookie(request, AUTHORIZATION_REQUEST_COOKIE_NAME)
			.map(cookie -> CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
		HttpServletResponse response) {
		if (authorizationRequest == null) {
			CookieUtil.deleteCookie(AUTHORIZATION_REQUEST_COOKIE_NAME, request, response);
			CookieUtil.deleteCookie(OAUTH2_STATE_COOKIE_NAME, request, response);
			return;
		}

		// OAuth2AuthorizationRequest와 상태 값(state)을 쿠키에 저장하는 로직
		CookieUtil.addStateCookie(response, AUTHORIZATION_REQUEST_COOKIE_NAME,
			CookieUtil.serialize(authorizationRequest),
			COOKIE_EXPIRE_SECONDS, true, true);

		String state = authorizationRequest.getState();
		CookieUtil.addStateCookie(response, OAUTH2_STATE_COOKIE_NAME,
			state,
			COOKIE_EXPIRE_SECONDS, true, true);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		// OAuth2AuthorizationRequest 및 상태 값을 제거
		return this.loadAuthorizationRequest(request);
	}

	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		// 쿠키에서 AuthorizationRequest 및 상태 값을 삭제하는 로직
		CookieUtil.deleteCookie(AUTHORIZATION_REQUEST_COOKIE_NAME, request, response);
		CookieUtil.deleteCookie(OAUTH2_STATE_COOKIE_NAME, request, response);
	}

}
