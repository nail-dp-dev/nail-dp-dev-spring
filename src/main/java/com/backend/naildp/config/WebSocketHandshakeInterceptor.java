package com.backend.naildp.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.backend.naildp.oauth2.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "handshake log")
@RequiredArgsConstructor
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtUtil jwtUtil;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpRequest = servletRequest.getServletRequest();
			String token = jwtUtil.getTokenFromRequest(httpRequest);
			token = jwtUtil.substringToken(token);
			log.info("Extracted token: {}", token);

			if (token != null && jwtUtil.validateToken(token)) {
				String username = jwtUtil.getUserInfoFromToken(token).getSubject();
				attributes.put("username", username); // 사용자 정보 설정
				log.info("User {}", username);
				return true;
			}
		}
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
	}

}