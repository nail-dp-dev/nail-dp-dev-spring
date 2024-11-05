package com.backend.naildp.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

	private final EmitterRepository emitterRepository;

	public SseEmitter createNewEmitter(String emitterKey, SseEmitter sseEmitter) {
		return emitterRepository.save(emitterKey, sseEmitter);
	}

	public void deleteEmitter(String emitterKey) {
		emitterRepository.deleteById(emitterKey);
	}

	public void sendPushNotificationToClient(String channel, String content) {
		emitterRepository.findById(channel)
			.ifPresent(sseEmitter -> sendPush(sseEmitter, channel, content));
	}

	/**
	 * emitter 로 푸시 알림 전송
	 */
	public void sendPush(SseEmitter emitter, String emitterKey, Object data) {
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
