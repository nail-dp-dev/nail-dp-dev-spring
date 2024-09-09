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

	private static final String[] PERMIT_URL_ARRAY = {
		"/auth/**",
		"/signup/",
		"/error"
	};
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

		if (StringUtils.hasText(tokenValue)) {
			// JWT 토큰 substring
			tokenValue = jwtUtil.substringToken(tokenValue);
			log.info(tokenValue);

			if (!jwtUtil.validateToken(tokenValue)) {
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
		// 사용자 유효성 검사
		// 헤더에 담긴 Access Token
		String expiredAccessToken = jwtUtil.getTokenFromRequest(req);
		String nickname = jwtUtil.getUserInfoFromToken(expiredAccessToken).getSubject();

		User findUser = userRepository.findUserByNickname(nickname).orElseThrow();

		// Refresh Token 추출
		// log.info("쿠키에서 리프레시 토큰 추출");
		String refreshTokenFromCooikie = "";
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshTokenFromCooikie = cookie.getValue();
					break;
				}
			}
		}
		log.info("refreshToken = " + refreshTokenFromCooikie);

		// Redis 에서 Refresh Token 추출
		String refreshTokenFromRedis = redisUtil.getRefreshToken(findUser.getNickname());

		// Refresh Token 유효성 검증
		if (!StringUtils.hasText(refreshTokenFromCooikie) || !jwtUtil.validateToken(refreshTokenFromCooikie)
			|| !refreshTokenFromRedis.equals(refreshTokenFromCooikie)) {
			log.info("Refresh Token 만료 또는 유효하지 않음");
			redisUtil.deleteRefreshToken(findUser.getNickname());
			res.sendError(401, "리프레시 토큰이 존재하지 않거나 만료됐습니다.");
			return;
		}

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