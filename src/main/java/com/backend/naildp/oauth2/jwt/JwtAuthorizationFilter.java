package com.backend.naildp.oauth2.jwt;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.naildp.entity.User;
import com.backend.naildp.oauth2.impl.UserDetailsServiceImpl;
import com.backend.naildp.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private static final String[] PERMIT_URL_ARRAY = {"/auth/**", "/signup/", "/error"};
	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;
	private final RedisUtil redisUtil;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws
		ServletException,
		IOException {

		if (Arrays.asList(PERMIT_URL_ARRAY).contains(req.getRequestURI())) {
			filterChain.doFilter(req, res);
			return;
		}

		String tokenValue = jwtUtil.getTokenFromRequest(req);
		log.info("Token from request: {}", tokenValue);

		log.info("1");

		if (StringUtils.hasText(tokenValue)) {
			log.info("2");

			// JWT 토큰 substring
			tokenValue = jwtUtil.substringToken(tokenValue);
			log.info(tokenValue);

			if (!jwtUtil.validateToken(tokenValue)) {
				log.info("3");

				log.error("Token Error");
				refreshAccessToken(req, res);
				return;
			}

			Claims info = jwtUtil.getUserInfoFromToken(tokenValue);

			try {
				setAuthentication(info.getSubject());
			} catch (Exception e) {
				throw e;
			}
		}

		filterChain.doFilter(req, res);
	}

	public void refreshAccessToken(HttpServletRequest req, HttpServletResponse res) throws IOException {

		String expiredAccessToken = jwtUtil.getTokenFromRequest(req);
		expiredAccessToken = jwtUtil.substringToken(expiredAccessToken);
		log.info("Token from request: {}", expiredAccessToken);

		String nickname;
		try {
			nickname = jwtUtil.getUserInfoFromToken(expiredAccessToken).getSubject();
		} catch (ExpiredJwtException e) {
			nickname = e.getClaims().getSubject(); // 만료된 토큰에서도 사용자 정보 추출 가능
		}
		log.info("4");

		User findUser = userRepository.findUserByNickname(nickname).orElseThrow(() ->
			new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		log.info("nickname = " + findUser.getNickname());

		// Refresh Token 추출
		log.info("쿠키에서 리프레시 토큰 추출");
		String refreshTokenFromCookie = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshTokenFromCookie = cookie.getValue();
					break;
				}
			}
		}
		jwtUtil.substringToken(refreshTokenFromCookie);
		log.info("refreshToken = " + refreshTokenFromCookie);

		// Redis 에서 Refresh Token 추출
		String refreshTokenFromRedis = redisUtil.getRefreshToken(findUser.getNickname());
		jwtUtil.substringToken(refreshTokenFromRedis);
		log.info("refreshTokenFromRedis: {}", refreshTokenFromRedis);

		if (refreshTokenFromRedis == null || !refreshTokenFromRedis.equals(refreshTokenFromCookie)) {
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다.");
			return;
		}
		log.info("5 ");

		// 리프레시 토큰 유효성 검증
		if (!jwtUtil.validateToken(refreshTokenFromCookie)) {
			redisUtil.deleteRefreshToken(findUser.getNickname());
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 만료되었습니다.");
			return;
		}
		log.info("6 ");

		// 새로운 AccessToken 발급
		log.info("새로운 Access Token 발급");
		String newAccessToken = jwtUtil.createToken(findUser.getNickname(), findUser.getRole());
	}

	// 인증 처리
	public void setAuthentication(String username) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = createAuthentication(username);
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);
	}

	// 인증 객체 생성
	private Authentication createAuthentication(String username) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}