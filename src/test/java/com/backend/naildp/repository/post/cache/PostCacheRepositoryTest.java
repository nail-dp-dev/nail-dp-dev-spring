package com.backend.naildp.repository.post.cache;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.backend.naildp.config.IntegrationTest;

@IntegrationTest
class PostCacheRepositoryTest {

	@Autowired
	private PostCacheRepository postCacheRepository;
	@Autowired
	private RedisTemplate<String, Long> longRedisTemplate;
	@Autowired
	private RedisTemplate<String, Boolean> booleanRedisTemplate;

	@AfterEach
	void tearDown() {
		Set<String> keys = longRedisTemplate.keys("*");
		if (keys != null && !keys.isEmpty()) {
			longRedisTemplate.delete(keys);
		}

		Set<String> keySetOfBooleanValue = booleanRedisTemplate.keys("*");
		if (keySetOfBooleanValue != null && !keySetOfBooleanValue.isEmpty()) {
			booleanRedisTemplate.delete(keys);
		}
	}

	@DisplayName("키에 해당하는 boolean 타입 값이 없다면 null 로 반환한다.")
	@Test
	void booleanValue() {
		//given
		String key = "testKey";

		//when
		Boolean value = booleanRedisTemplate.opsForValue().get(key);

		//then
		assertThat(value).isNull();
	}

	@DisplayName("등록되지 않은 키로 redis 값 조회")
	@Test
	void saveEmptySet() {
		//given
		String key = "testKey";

		//when
		Set<?> members = longRedisTemplate.opsForSet().members(key);

		//then
		assertThat(members).isNotNull();
		assertThat(members).isEmpty();
	}

	@DisplayName("캐시 저장 테스트")
	@Test
	void cacheSave() {
		//given
		String key = "testKey";
		HashSet<Long> value = new HashSet<>();
		value.add(1L);
		value.add(2L);
		value.add(3L);

		postCacheRepository.save(key, value);

		//when
		Set<Long> postIdSet = postCacheRepository.findPostIdSet(key);

		//then
		assertThat(postIdSet).containsExactly(1L, 2L, 3L);
	}

	@DisplayName("캐시에 빈 set 저장은 저장되지 않는다.")
	@Test
	void saveEmptySetInCache() {
		//given
		String key = "testKey";
		HashSet<Long> value = new HashSet<>();

		postCacheRepository.save(key, value);

		//when
		Set<Long> postIdSet = postCacheRepository.findPostIdSet(key);

		//then
		assertThat(postIdSet).isNotNull();
		assertThat(postIdSet).isEmpty();
	}

	@DisplayName("캐시에 키가 없을 때는 빈 set을 반환한다.")
	@Test
	void cacheReturnEmptySetWhenNotExistingKey() {
		//given
		String key = "testKey";

		//when
		Set<Long> postIdSet = postCacheRepository.findPostIdSet(key);

		//then
		assertThat(postIdSet).isNotNull();
		assertThat(postIdSet).isEmpty();
	}

}