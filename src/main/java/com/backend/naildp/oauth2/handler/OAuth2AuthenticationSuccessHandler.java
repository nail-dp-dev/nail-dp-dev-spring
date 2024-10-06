package com.backend.naildp.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.oauth2.jwt.RedisUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final CookieUtil cookieUtil;
	private final JwtUtil jwtUtil;
	private final RedisUtil redisUtil;
	private static String domain;

	@Value("${spring.server.domain}")
	public void setDomain(String valueDomain) {
		domain = valueDomain;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		UserDetailsImpl userDetails = getOAuth2UserPrincipal(authentication);

		// 유저 정보가 있는 경우(JWT 토큰을 생성하여 쿠키에 추가)
		String token = jwtUtil.createToken(userDetails.getUser().getNickname(), userDetails.getUser().getRole());
		jwtUtil.addJwtToCookie(token, "Authorization", response);
		jwtUtil.addJwtToCookie(jwtUtil.createRefreshToken(), "refreshToken", response);

		redisUtil.saveRefreshToken(userDetails.getUser().getNickname(), jwtUtil.createRefreshToken());

		getRedirectStrategy().sendRedirect(request, response, domain);
	}

	private UserDetailsImpl getOAuth2UserPrincipal(Authentication authentication) {
		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetailsImpl) {
			return (UserDetailsImpl)principal;
		}
		return null;
	}

}
