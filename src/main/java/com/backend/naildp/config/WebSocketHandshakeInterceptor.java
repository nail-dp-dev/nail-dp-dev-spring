// package com.backend.naildp.config;
//
// import java.net.URLDecoder;
// import java.util.Arrays;
// import java.util.Map;
// import java.util.Optional;
//
// import org.springframework.http.server.ServerHttpRequest;
// import org.springframework.http.server.ServerHttpResponse;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketHandler;
// import org.springframework.web.socket.server.HandshakeInterceptor;
//
// import com.backend.naildp.exception.TokenNotValidateException;
// import com.backend.naildp.oauth2.jwt.JwtUtil;
// import com.backend.naildp.repository.UserRepository;
//
// import jakarta.servlet.http.HttpServletRequest;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @RequiredArgsConstructor
// @Component
// public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
//
// 	private final JwtUtil jwtUtil;
// 	private final UserRepository userRepository;
//
// 	@Override
// 	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
// 		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//
// 		if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
// 			HttpServletRequest httpRequest = servletRequest.getServletRequest();
//
// 			// 쿠키에서 토큰 추출
// 			String token = getTokenFromCookie(httpRequest);
//
// 			if (token != null) {
// 				try {
// 					token = jwtUtil.substringToken(token);
//
// 					if (jwtUtil.validateToken(token)) {
// 						String username = jwtUtil.getUserInfoFromToken(token).getSubject();
//
// 						userRepository.findUserByNickname(username)
// 							.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
// 						attributes.put("username", username);
// 						log.info("WebSocket 연결 허용 - 사용자: {}", username);
// 						return true;
// 					}
// 				} catch (TokenNotValidateException e) {
// 					log.error("유효하지 않은 JWT 토큰", e);
// 				}
// 			}
// 		}
//
// 		log.warn("WebSocket 연결 거부 - 유효하지 않은 토큰");
// 		response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
// 		return false;
// 	}
//
// 	@Override
// 	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
// 		WebSocketHandler wsHandler, Exception exception) {
// 	}
//
// 	private String getTokenFromCookie(HttpServletRequest request) {
// 		String authorizationHeader = JwtUtil.AUTHORIZATION_HEADER;
// 		return Optional.ofNullable(request.getCookies())
// 			.stream()
// 			.flatMap(Arrays::stream)
// 			.filter(cookie -> authorizationHeader.equals(cookie.getName()))
// 			.findFirst()
// 			.map(cookie -> {
// 				try {
// 					return URLDecoder.decode(cookie.getValue(), "UTF-8");
// 				} catch (Exception e) {
// 					log.error("JWT 쿠키 디코딩 실패", e);
// 					return null;
// 				}
// 			})
// 			.orElse(null);
// 	}
// }
