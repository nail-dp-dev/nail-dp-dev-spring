package com.backend.naildp.service;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.dto.PushNotificationResponseDto;
import com.backend.naildp.dto.notification.PushNotificationDto;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.repository.EmitterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

	private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 30분

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final RedisOperations<String, PushNotificationResponseDto> eventRedisOperations;
	private final ObjectMapper objectMapper;
	private final EmitterRepository emitterRepository;
	private final RedisMessageService redisMessageService;

	public SseEmitter subscribe(String username, String lastEventId) {
		String emitterKey = username;
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		emitterRepository.save(emitterKey, emitter);

		// // MessageListener 익명함수 사용해서 onMessage 구현, Redis 에서 새로운 알림이 발생하면 자동적으로 onMessage 가 호출
		// // 즉 알림을 serialize 하고 해당 Client 에게 알림을 전송한다.
		// final MessageListener messageListener = (message, pattern) -> {
		// 	final PushNotificationResponseDto notificationResponse = serialize(message);
		// 	sendPush(emitter, emitterKey, notificationResponse);
		// };
		//
		// // redisMessageListenerContainer 에 새로운 MessageListener 를 추가함
		// redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(getChannelName(emitterKey)));

		redisMessageService.subscribe(emitterKey);

		// // emitter 의 상태를 체크함, 완료되었는지 타임아웃이 났는지
		// checkEmitterStatus(emitter, messageListener, emitterKey);

		checkEmitterStatusV2(emitter, emitterKey);

		String dummyData = "EventStream Created. [username=" + username + "]";
		sendPush(emitter, emitterKey, dummyData);

		return emitter;
	}

	public void checkEmitterStatus(final SseEmitter emitter, final MessageListener messageListener, String emitterKey) {
		emitter.onTimeout(() -> {
			log.info("sse timeout");
			emitterRepository.deleteById(emitterKey);
			redisMessageListenerContainer.removeMessageListener(messageListener);
		});
		emitter.onCompletion(() -> {
			log.info("sse complete");
			emitterRepository.deleteById(emitterKey);
			redisMessageListenerContainer.removeMessageListener(messageListener);
		});
	}

	public void checkEmitterStatusV2(final SseEmitter emitter, String emitterKey) {
		emitter.onTimeout(() -> {
			log.info("sse timeout");
			emitterRepository.deleteById(emitterKey);
			redisMessageService.removeSubscribe(emitterKey);
		});
		emitter.onCompletion(() -> {
			log.info("sse complete");
			emitterRepository.deleteById(emitterKey);
			redisMessageService.removeSubscribe(emitterKey);
		});
	}

	/**
	 * 팔로우 알림 전송 : convertAndSend() 로 해당 채널에 데이터를 전송한다.
	 */
	public void sendPushNotification(Notification notification) {
		this.eventRedisOperations.convertAndSend(getChannelName(notification.getReceiver().getNickname()),
			PushNotificationResponseDto.fromV2(notification));
	}

	public void sendPushNotificationV2(Notification notification) {
		log.info("푸시알림 v2");
		log.info("receiver nickname : {}", notification.getReceiver().getNickname());
		redisMessageService.publish(notification.getReceiver().getNickname(),
			PushNotificationDto.from(notification));
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
			return this.objectMapper.readValue(message.getBody(), PushNotificationResponseDto.class);
		} catch (IOException e) {
			throw new IllegalArgumentException("알림 serialize 실패");
		}
	}

	private String getChannelName(final String username) {
		return "topics:" + username;
	}
}
