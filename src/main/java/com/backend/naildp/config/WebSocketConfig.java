package com.backend.naildp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompHandler stompHandler;
	private final WebSocketHandshakeInterceptor handshakeInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-stomp")
			.setAllowedOriginPatterns("*")
			.addInterceptors(handshakeInterceptor)
			.withSockJS();

	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지를 브로드캐스팅할 때 사용할 prefix 설정
		registry.enableSimpleBroker("/sub");
		// 클라이언트에서 메시지를 보낼 때 사용할 prefix 설정
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}
}
