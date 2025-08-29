package com.backend.naildp.repository.post.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostCacheRepository {

	@Qualifier(value = "stringLongRedisTemplate")
	private final RedisTemplate<String, Long> redisTemplate;

	public Set<Long> findPostIdSet(String key) {
		return redisTemplate.opsForSet().members(key);
	}

	public void save(String key, Set<Long> likedPostIdSet) {
		SetOperations<String, Long> setOperations = redisTemplate.opsForSet();
		Long[] idSetArray = likedPostIdSet.toArray(new Long[0]);

		setOperations.add(key, idSetArray);

		redisTemplate.expire(key, Duration.of(30, ChronoUnit.MINUTES));
	}
}
