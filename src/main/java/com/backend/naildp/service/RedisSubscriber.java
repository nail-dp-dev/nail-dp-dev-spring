package com.backend.naildp.service;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.notification.PushNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SseEmitterService sseEmitterService;

	/**
	 * redis 에서 특정 채널에 발행된 메시지를 처리하는 메서드
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());
		log.info("message channel : {}", channel);

		PushNotificationDto pushNotificationDto = serialize(message);
		log.info("receiver : {}, sender : {}", pushNotificationDto.getReceiverNickname(),
			pushNotificationDto.getSenderNickname());

		sseEmitterService.sendPushNotificationToClient(channel, pushNotificationDto.getContent());
	}

	private PushNotificationDto serialize(Message message) {
		try {
			return this.objectMapper.readValue(message.getBody(), PushNotificationDto.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("알림 serialize 실패");
		}
	}

}
