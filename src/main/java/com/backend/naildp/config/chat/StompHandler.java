package com.backend.naildp.config.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.service.chat.ChatRoomStatusService;
import com.backend.naildp.service.chat.MessageStatusService;
import com.backend.naildp.service.chat.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "StompHandler log")
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final SessionService sessionService;
	private final ChatRoomStatusService chatRoomStatusService;
	private final MessageStatusService messageStatusService;

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
					String destination = headerAccessor.getDestination();
					if (destination.startsWith("/sub/chat/list")) {
						handleChatListSubscription(headerAccessor);
					} else if (destination.startsWith("/sub/chat/")) {
						handleChatRoomSubscription(headerAccessor);
					}
					break;
				case UNSUBSCRIBE:
					handleExit(headerAccessor);
					break;
				case DISCONNECT:
					sessionService.deleteSession(headerAccessor.getSessionId());
					break;
			}
		} catch (Exception e) {
			throw new MessageDeliveryException("메세지 요청 오류");
		}
	}

	private void handleChatListSubscription(StompHeaderAccessor headerAccessor) throws Exception {
		String roomId = extractRoomId(headerAccessor.getDestination());
		String userId = sessionService.getUserIdBySessionId(headerAccessor.getSessionId());

		log.info("사용자 {} 가 채팅목록 {} 에 입장함", userId, roomId);

	}

	private void handleChatRoomSubscription(StompHeaderAccessor headerAccessor) throws Exception {
		String roomId = extractRoomId(headerAccessor.getDestination());
		String userId = sessionService.getUserIdBySessionId(headerAccessor.getSessionId());

		log.info("사용자 {} 가 채팅방 {} 에 입장함", userId, roomId);

		chatRoomStatusService.resetUnreadCount(roomId, userId);
		messageStatusService.resetFirstUnreadMessageId(roomId, userId);

	}

	private void handleExit(StompHeaderAccessor headerAccessor) {
		String roomId = extractRoomId(headerAccessor.getDestination());
		String userId = sessionService.getUserIdBySessionId(headerAccessor.getSessionId());

		log.info("사용자 {} 가 채팅방 {} 에서 퇴장함", userId, roomId);
	}

	private String extractRoomId(String destination) {
		if (destination == null) {
			throw new MessageDeliveryException("not found destination");
		}
		return destination.replace("/sub/chat/", "");
	}

}