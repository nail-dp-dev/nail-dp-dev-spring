package com.backend.naildp.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.dto.PushNotificationResponseDto;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.repository.EmitterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 2분

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final RedisOperations<String, PushNotificationResponseDto> eventRedisOperations;
	private final ObjectMapper objectMapper;
	private final EmitterRepository emitterRepository;

	public SseEmitter subscribe(String username, String lastEventId) {
		String emitterKey = username;
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitterRepository.save(emitterKey, emitter);

		emitter.onTimeout(() -> {
			log.info("sse timeout : id={}", username);
			emitterRepository.deleteById(emitterKey);
		});

		emitter.onCompletion(() -> {
			log.info("sse complete : id={}", username);
			emitter.complete();
			emitterRepository.deleteById(emitterKey);
		});

		emitter.onError(e -> {
			log.info("sse error occurred : id={}, message={}", username, e.getMessage());
			emitterRepository.deleteById(emitterKey);
		});

		String dummyData = "EventStream Created. [username=" + username + "]";
		sendPush(emitter, emitterKey, dummyData);

		// MessageListener 익명함수 사용해서 onMessage 구현, Redis 에서 새로운 알림이 발생하면 자동적으로 onMessage 가 호출
		// 즉 알림을 serialize 하고 해당 Client 에게 알림을 전송한다.
		final MessageListener messageListener = (message, pattern) -> {
			final PushNotificationResponseDto notificationResponse = serialize(message);
			sendPush(emitter, emitterKey, notificationResponse);
		};

		// redisMessageListenerContainer 에 새로운 MessageListener 를 추가함
		redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(getChannelName(emitterKey)));

		// emitter 의 상태를 체크함, 완료되었는지 타임아웃이 났는지
		checkEmitterStatus(emitter, messageListener, emitterKey);

		return emitter;
	}

	public void sendFollowPush(String username, Notification notification) {
		log.info("팔로우 알림 전송 : {}" , username);
		this.eventRedisOperations.convertAndSend(getChannelName(username),
			PushNotificationResponseDto.from(notification));
		log.info("팔로우 알림 전송 성공 : {}" , username);
	}

	private void sendPush(SseEmitter emitter, String emitterKey, Object data) {
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

	private PushNotificationResponseDto serialize(final Message message) {
		try {
			// final Notification notification = this.objectMapper.readValue(message.getBody(), Notification.class);
			// return PushNotificationResponseDto.from(notification);
			return this.objectMapper.readValue(message.getBody(), PushNotificationResponseDto.class);
		} catch (IOException e) {
			// throw new InvalidRedisMessageException(message);
			throw new IllegalArgumentException("알림 serialize 실패");
		}
	}

	private void checkEmitterStatus(final SseEmitter emitter, final MessageListener messageListener, String emitterKey) {
		emitter.onCompletion(() -> {
			emitterRepository.deleteById(emitterKey);
			redisMessageListenerContainer.removeMessageListener(messageListener);
		});
		emitter.onTimeout(() -> {
			emitterRepository.deleteById(emitterKey);
			redisMessageListenerContainer.removeMessageListener(messageListener);
		});
	}

	private String getChannelName(final String username) {
		return "topics:" + username;
	}
}