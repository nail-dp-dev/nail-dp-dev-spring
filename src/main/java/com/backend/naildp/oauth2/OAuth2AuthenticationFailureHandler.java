package com.backend.naildp.oauth2;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.exception.SignUpRequiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	private static final String SIGNUP_URI = "http://localhost:3000/sign-up";

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		if (exception instanceof SignUpRequiredException) {
			// 회원가입이 필요한 경우 signup 페이지로 리다이렉트
			getRedirectStrategy().sendRedirect(request, response, SIGNUP_URI);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			ApiResponse<?> errorResponse = ApiResponse.of(401);
			errorResponse.setMessage(exception.getMessage());

			ObjectMapper objectMapper = new ObjectMapper();
			String jsonResponse = objectMapper.writeValueAsString(errorResponse);

			response.getWriter().write(jsonResponse);
		}
	}
}
