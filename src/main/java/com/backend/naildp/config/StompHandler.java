package com.backend.naildp.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "StompHandler log")
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final SessionService sessionService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		handleMessage(headerAccessor);
		log.info("stomp command : {} , destination :  {} , sessionId : {}", headerAccessor.getCommand(),
			headerAccessor.getDestination(), headerAccessor.getSessionId());
		return message;
	}

	private void handleMessage(StompHeaderAccessor headerAccessor) {
		if (headerAccessor == null || headerAccessor.getCommand() == null) {
			throw new MessageDeliveryException("chat:  요청이 잘못되었습니다.");
		}
		try {
			switch (headerAccessor.getCommand()) {
				case CONNECT:
					String username = (String)headerAccessor.getSessionAttributes().get("username");
					sessionService.saveSession(headerAccessor.getSessionId(), username);
					break;
				case SUBSCRIBE:
					enterToChatRoom(headerAccessor);
					break;
				case UNSUBSCRIBE:
					exitToChatRoom(headerAccessor);
					break;
				case DISCONNECT:
					sessionService.deleteSession(headerAccessor.getSessionId());
					break;
			}
		} catch (Exception e) {
			throw new MessageDeliveryException("메세지 요청 오류");
		}
	}

	private void enterToChatRoom(StompHeaderAccessor headerAccessor) throws Exception {
		String memberId = sessionService.getMemberIdBySessionId(headerAccessor.getSessionId());
		String roomId = extractRoomId(headerAccessor.getDestination());
	}

	private void exitToChatRoom(StompHeaderAccessor headerAccessor) {
		String memberId = sessionService.getMemberIdBySessionId(headerAccessor.getSessionId());
		String roomId = extractRoomId(headerAccessor.getDestination());
	}

	private String extractRoomId(String destination) {
		if (destination == null) {
			throw new MessageDeliveryException("메세지 오류");
		}
		return destination.replace("/sub/", "");
	}

}