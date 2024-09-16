package com.backend.naildp.oauth2.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisUtil {

	private final RedisTemplate<String, String> redisTemplate;

	public void saveRefreshToken(String nickname, String refreshToken) {
		redisTemplate.opsForValue().set(nickname, refreshToken);
	}

	public String getRefreshToken(String nickname) {
		return redisTemplate.opsForValue().get(nickname);
	}

	public void deleteRefreshToken(String nickname) {
		redisTemplate.delete(nickname);
	}
}