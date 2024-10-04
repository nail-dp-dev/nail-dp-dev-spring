package com.backend.naildp.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.repository.UserRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "StompHandler log")
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 헤더에서 Authorization 정보를 가져옴
			String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
			log.error("Authorization header is missing or invalid");

			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
				String jwtToken = authorizationHeader.substring(7); // "Bearer " 부분 제거

				if (jwtUtil.validateToken(jwtToken)) {
					Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
					String nickname = claims.getSubject();

					User user = userRepository.findByNickname(nickname).orElseThrow(() ->
						new CustomException("notFound", ErrorCode.NOT_FOUND)
					);

					// Spring Security 인증 객체 생성
					UserDetailsImpl userDetails = new UserDetailsImpl(user);
					UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

					// 인증 정보를 SecurityContextHolder에 설정
					SecurityContextHolder.getContext().setAuthentication(authentication);

					// WebSocket 세션에 사용자 정보를 설정
					accessor.setUser(authentication);
				} else {
					log.error("Invalid JWT Token");
					throw new AuthenticationCredentialsNotFoundException("JWT token is invalid");
				}
			} else {
				log.error("Authorization header is missing or invalid");
				throw new AuthenticationCredentialsNotFoundException("Authorization header is missing or invalid");
			}
		}

		return message;
	}

}
