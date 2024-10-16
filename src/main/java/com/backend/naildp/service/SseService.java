package com.backend.naildp.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.dto.PushNotificationResponseDto;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private static final long DEFAULT_TIMEOUT = 1 * 60 * 1000L; // 2분

	private final EmitterRepository emitterRepository;

	public SseEmitter subscribe(String nickname, String lastEventId) {
		String emitterKey = nickname;
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitterRepository.save(emitterKey, emitter);

		emitter.onTimeout(() -> {
			log.info("sse timeout : id={}", nickname);
			emitterRepository.deleteById(emitterKey);
		});

		emitter.onCompletion(() -> {
			log.info("sse complete : id={}", nickname);
			emitter.complete();
			emitterRepository.deleteById(emitterKey);
		});

		emitter.onError(e -> {
			log.info("sse error occurred : id={}, message={}", nickname, e.getMessage());
			emitterRepository.deleteById(emitterKey);
		});

		String dummyData = "EventStream Created. [username=" + nickname + "]";
		sendPush(emitter, emitterKey, dummyData);

		return emitter;
	}

	public void sendFollowPush(String nickname, Notification notification) {
		log.info("팔로우 알림 전송 : {}" , nickname);
		String eventId = nickname + "_" + System.currentTimeMillis();

		emitterRepository.findById(nickname)
			.ifPresent(emitter -> {
				log.info("emitter find!");
				sendPush(emitter, nickname, PushNotificationResponseDto.create(nickname, notification));
			});
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
}
