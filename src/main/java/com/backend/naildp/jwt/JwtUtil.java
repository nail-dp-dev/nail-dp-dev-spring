package com.backend.naildp.jwt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.exception.TokenNotValidateException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtUtil {

	//JWT 데이터

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AUTHORIZATION_KEY = "auth";

	public static final String BEARER_PREFIX = "Bearer ";

	private final long TOKEN_TIME = 30 * 24 * 60 * 60 * 1000L; //60분l

	@Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
	private String secretKey;
	private Key key;
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	// 로그 설정
	public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

	@PostConstruct // 한번만 받아오면 되는 값을 가져올때, 새로운 요청 방지
	public void init() {
		byte[] bytes = Base64.getDecoder().decode(secretKey);
		key = Keys.hmacShaKeyFor(bytes);
	}

	//JWT 생성
	// 토큰 생성
	public String createToken(String username, UserRole role) {
		Date date = new Date();

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(username) // 사용자 식별자값(ID)
				.claim(AUTHORIZATION_KEY, role) // 사용자 권한
				.setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
				.setIssuedAt(date) // 발급일
				.signWith(key, signatureAlgorithm) // 암호화 알고리즘
				.compact();
	}

	// JWT Cookie 에 저장
	public void addJwtToCookie(String token, HttpServletResponse res) {
		try {
			token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

			Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
			cookie.setPath("/");

			// Response 객체에 Cookie 추가
			res.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
	}

	// Cookie에 들어있던 JWT 토큰을 Substring
	public String substringToken(String tokenValue) {
		if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) { //공백, null x bearer인지
			return tokenValue.substring(7);
		}
		logger.error("Not Found Token");
		throw new TokenNotValidateException("Not Found Token");
	}

	// JWT 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException | SignatureException e) {
			logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
			throw new TokenNotValidateException("잘못된 JWT 서명입니다.", e);
		} catch (ExpiredJwtException e) {
			logger.error("Expired JWT token, 만료된 JWT token 입니다.");
			throw new TokenNotValidateException("만료된 JWT 토큰입니다.", e);
		} catch (UnsupportedJwtException e) {
			logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
			throw new TokenNotValidateException("지원되지 않는 JWT 토큰입니다.", e);
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
			throw new TokenNotValidateException("잘못된 JWT 토큰 입니다.", e);

		}
	}

	// JWT에서 사용자 정보 가져오기
	public Claims getUserInfoFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	// HttpServletRequest 에서 Cookie Value : JWT 가져오기
	public String getTokenFromRequest(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
					try {
						return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
					} catch (UnsupportedEncodingException e) {
						return null;
					}
				}
			}

		}
		return null;
	}

}
