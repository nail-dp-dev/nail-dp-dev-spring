package com.backend.naildp.oauth2;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.backend.naildp.common.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	private static final String OAUTH2_STATE_COOKIE_NAME = "oauth2_state";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

		log.info("oauth2authorizationRequest111111");
		return CookieUtil.getStateCookie(request, AUTHORIZATION_REQUEST_COOKIE_NAME)
			.map(cookie -> CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
		HttpServletResponse response) {
		log.info("oauth2authorizationRequest222222");

		if (authorizationRequest == null) {
			CookieUtil.deleteCookie(AUTHORIZATION_REQUEST_COOKIE_NAME, request, response);
			CookieUtil.deleteCookie(OAUTH2_STATE_COOKIE_NAME, request, response);
			return;
		}

		CookieUtil.addStateCookie(response, AUTHORIZATION_REQUEST_COOKIE_NAME,
			CookieUtil.serialize(authorizationRequest),
			COOKIE_EXPIRE_SECONDS, true, "None");

		String state = authorizationRequest.getState();
		CookieUtil.addStateCookie(response, OAUTH2_STATE_COOKIE_NAME,
			state,
			COOKIE_EXPIRE_SECONDS, true, "None");
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		log.info("oauth2authorizationRequest222222");

		OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
		if (authorizationRequest != null) {
			CookieUtil.deleteCookie(AUTHORIZATION_REQUEST_COOKIE_NAME, request, response);
			CookieUtil.deleteCookie(OAUTH2_STATE_COOKIE_NAME, request, response);
		}
		return authorizationRequest;
	}
}
