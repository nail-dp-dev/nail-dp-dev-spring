package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

	private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 30분

	private final SseEmitterService sseEmitterService;
	private final RedisMessageService redisMessageService;

	public SseEmitter subscribe(String username) {
		String emitterKey = username;
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		sseEmitterService.createNewEmitter(emitterKey, emitter);
		checkEmitterStatus(emitter, emitterKey);

		redisMessageService.subscribe(emitterKey);

		String dummyData = "EventStream Created. [username=" + username + "]";
		sseEmitterService.sendPush(emitter, emitterKey, dummyData);

		return emitter;
	}

	public void checkEmitterStatus(final SseEmitter emitter, String emitterKey) {
		emitter.onTimeout(() -> {
			log.info("sse timeout");
			emitter.complete();
		});
		emitter.onError(e -> {
			log.info("sse error: {}", e.getMessage());
			emitter.complete();
		});
		emitter.onCompletion(() -> {
			log.info("sse complete");
			sseEmitterService.deleteEmitter(emitterKey);
			redisMessageService.removeSubscribe(emitterKey);
		});
	}
}
