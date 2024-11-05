package com.backend.naildp.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {

	private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

	public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
		emitterMap.put(emitterId, sseEmitter);
		return sseEmitter;
	}

	public Optional<SseEmitter> findById(String nickname) {
		return Optional.ofNullable(emitterMap.get(nickname));
	}

	public void deleteById(String emitterId) {
		emitterMap.remove(emitterId);
	}
}
