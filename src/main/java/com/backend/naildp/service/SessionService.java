package com.backend.naildp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {
	private final RedisTemplate<String, String> redisSessionTemplate;
	private final String KEY = "sessionStore";

	public void saveSession(String sessionId, String userId) {
		redisSessionTemplate.opsForSet().add(KEY, sessionId);
		redisSessionTemplate.opsForValue().set(generateSessionKey(sessionId), userId);
	}

	public String getMemberIdBySessionId(String sessionId) {
		return redisSessionTemplate.opsForValue().get(generateSessionKey(sessionId));
	}

	public void deleteSession(String sessionId) {
		redisSessionTemplate.opsForSet().remove(KEY, sessionId);
		redisSessionTemplate.delete(generateSessionKey(sessionId));
	}

	private String generateSessionKey(String sessionId) {
		return String.format("%s:%s", KEY, sessionId);
	}
}