package com.backend.naildp.service;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.dto.notification.PushNotificationDto;
import com.backend.naildp.repository.EmitterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final EmitterRepository emitterRepository;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());
		log.info("message channel : {}", channel);

		PushNotificationDto pushNotificationDto = serialize(message);
		log.info("receiver : {}, sender : {}", pushNotificationDto.getReceiverNickname(), pushNotificationDto.getSenderNickname());

		sendPush(channel, pushNotificationDto.getContent());
	}

	private String serializeChannel(Message message) {
		try {
			return objectMapper.readValue(message.getChannel(), String.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("알림 채널 serialize 실패");
		}
	}

	private PushNotificationDto serialize(Message message) {
		try {
			return this.objectMapper.readValue(message.getBody(), PushNotificationDto.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("알림 serialize 실패");
		}
	}

	private void sendPush(String emitterKey, Object data) {
		emitterRepository.findById(emitterKey)
			.ifPresent(emitter -> send(emitter, emitterKey, data));
	}

	private void send(SseEmitter emitter, String emitterKey, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(emitterKey)
				.name("sse")
				.data(data));
		} catch (IOException e) {
			log.info("푸시 알림 전송 중 SSE 연결 오류 발생");
			emitterRepository.deleteById(emitterKey);
		}
	}
}
