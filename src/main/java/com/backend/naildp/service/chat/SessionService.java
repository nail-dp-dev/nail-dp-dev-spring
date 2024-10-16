package com.backend.naildp.service.chat;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
	private final RedisTemplate<String, String> redisSessionTemplate;
	private final String KEY = "sessionStore";

	public void saveSession(String sessionId, String userId) {
		redisSessionTemplate.opsForSet().add(KEY, sessionId);
		redisSessionTemplate.opsForValue().set(generateSessionKey(sessionId), userId);
		redisSessionTemplate.opsForValue().set(generateSessionKey(userId), sessionId);
	}

	public String getUserIdBySessionId(String sessionId) {
		return redisSessionTemplate.opsForValue().get(generateSessionKey(sessionId));
	}

	public Boolean isSessionExist(String userId) {
		log.info(String.valueOf(redisSessionTemplate.hasKey(generateSessionKey(userId))));
		return redisSessionTemplate.hasKey(generateSessionKey(userId));
	}

	public void deleteSession(String sessionId) {
		redisSessionTemplate.opsForSet().remove(KEY, sessionId);
		redisSessionTemplate.delete(generateSessionKey(sessionId));
	}

	private String generateSessionKey(String sessionId) {
		return String.format("%s:%s", KEY, sessionId);
	}
}