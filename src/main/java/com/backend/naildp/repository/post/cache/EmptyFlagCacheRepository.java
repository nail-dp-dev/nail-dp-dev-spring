package com.backend.naildp.repository.post.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmptyFlagCacheRepository {

	@Qualifier(value = "emptyFlagRedisTemplate")
	private final RedisTemplate<String, Boolean> redisTemplate;

	public Optional<Boolean> getFlag(String key) {
		Boolean value = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(value);
	}

	public void saveFlag(String key, boolean value) {
		redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
	}

	public void saveTrue(String key) {
		saveFlag(key, true);
	}

	public void saveFalse(String key) {
		saveFlag(key, false);
	}
}
