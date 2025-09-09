package com.backend.naildp.service.post;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import com.backend.naildp.config.IntegrationTest;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.post.cache.EmptyFlagCacheRepository;
import com.backend.naildp.repository.post.cache.PostCacheRepository;

@IntegrationTest
class PostCacheManagerTest {

	@Autowired
	private PostCacheManager postCacheManager;

	@Autowired
	private RedisTemplate<String, Long> longValueRedisTemplate;

	@Autowired
	private RedisTemplate<String, Boolean> booleanValueRedisTemplate;

	@SpyBean
	private PostCacheRepository postCacheRepository;

	@SpyBean
	EmptyFlagCacheRepository emptyFlagCacheRepository;

	@MockBean
	private PostRepository postRepository;

	@AfterEach
	void tearDown() {
		Set<String> keySetOfLongValue = longValueRedisTemplate.keys("*");
		if (keySetOfLongValue != null && !keySetOfLongValue.isEmpty()) {
			longValueRedisTemplate.delete(keySetOfLongValue);
		}

		Set<String> keySetOfBooleanValue = booleanValueRedisTemplate.keys("*");
		if (keySetOfBooleanValue != null && !keySetOfBooleanValue.isEmpty()) {
			booleanValueRedisTemplate.delete(keySetOfBooleanValue);
		}
	}

	@DisplayName("캐시에 데이터가 있다면 그대로 반환한다.")
	@Test
	void cacheSave() {
		//given
		String username = "testUser";

		String key = "testKey";
		HashSet<Long> value = new HashSet<>();
		value.add(1L);
		value.add(2L);
		value.add(3L);

		postCacheRepository.save(key, value);

		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		when(postRepository.findLikedPostIds(username)).thenReturn(List.of(1L, 2L, 3L));

		//when
		Set<Long> set = postCacheManager.getOrLoadPosts(username, key, dataLoaderFromDatabase);

		//then
		assertThat(set).containsExactly(1L, 2L, 3L);
		verify(postRepository, never()).findLikedPostIds(any());
	}

	@DisplayName("캐시에 키가 없으면 DB에서 데이터를 찾아 캐시에 저장한다.")
	@Test
	void notExistingKeyRenewCache() {
		//given
		String username = "testUser";
		String key = "testKey";

		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		when(postRepository.findLikedPostIds(username)).thenReturn(List.of(1L, 2L, 3L));

		//when
		Set<Long> set = postCacheManager.getOrLoadPosts(username, key, dataLoaderFromDatabase);

		//then
		assertThat(set).containsExactly(1L, 2L, 3L);
		verify(postRepository).findLikedPostIds(eq(username));
	}

	@DisplayName("emptyFlag가 null 이면 실제 데이터를 조회한다.")
	@Test
	void ifEmptyFlagIsNullThenCallDatabase() {
		//given
		String username = "testUser";
		String cacheKey = CacheKeyGenerator.getLikedPostsKey(username);
		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		String emptyFlagKey = CacheKeyGenerator.generateEmptyFlagKey(cacheKey);

		when(postRepository.findLikedPostIds(username)).thenReturn(List.of(1L, 2L, 3L));

		//when
		Set<Long> postIdSet = postCacheManager.getOrLoadPostsV3(username, cacheKey, dataLoaderFromDatabase);

		//then
		assertThat(postIdSet).containsExactly(1L, 2L, 3L);

		verify(postRepository).findLikedPostIds(eq(username));
		verify(emptyFlagCacheRepository).saveFalse(eq(emptyFlagKey));
		verify(postCacheRepository).save(eq(cacheKey), eq(postIdSet));
	}

	@DisplayName("emptyFlag가 true라면 실제 데이터를 조회하지 않고 빈 셋을 반환한다.")
	@Test
	void ifEmptyFlagIsTrueThenReturnEmptySet() {
		//given
		String username = "testUser";
		String cacheKey = CacheKeyGenerator.getLikedPostsKey(username);
		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		String emptyFlagKey = CacheKeyGenerator.generateEmptyFlagKey(cacheKey);

		booleanValueRedisTemplate.opsForValue().set(emptyFlagKey, true);

		//when
		Set<Long> postIdSet = postCacheManager.getOrLoadPostsV3(username, cacheKey, dataLoaderFromDatabase);

		//then
		assertThat(postIdSet).isEmpty();

		verify(postRepository, never()).findLikedPostIds(any());
	}

	@DisplayName("emptyFlag가 false라면 실제 데이터를 조회하지 않고 캐시 데이터를 반환한다.")
	@Test
	void ifEmptyFlagIsFalseThenReturnCache() {
		//given
		String username = "testUser";
		String cacheKey = CacheKeyGenerator.getLikedPostsKey(username);
		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		String emptyFlagKey = CacheKeyGenerator.generateEmptyFlagKey(cacheKey);

		longValueRedisTemplate.opsForSet().add(cacheKey, 1L, 2L, 3L);
		booleanValueRedisTemplate.opsForValue().set(emptyFlagKey, false);

		//when
		Set<Long> postIdSet = postCacheManager.getOrLoadPostsV3(username, cacheKey, dataLoaderFromDatabase);

		//then
		assertThat(postIdSet).containsExactly(1L, 2L, 3L);

		verify(postCacheRepository).findPostIdSet(eq(cacheKey));
	}

	@DisplayName("DB에서 빈 결과가 나오면 emptyFlag는 true가 된다.")
	@Test
	void ifDatabaseReturnEmptyThenEmptyFlagSetTrue() {
		//given
		String username = "testUser";
		String cacheKey = CacheKeyGenerator.getLikedPostsKey(username);
		Supplier<List<Long>> dataLoaderFromDatabase = () -> postRepository.findLikedPostIds(username);
		String emptyFlagKey = CacheKeyGenerator.generateEmptyFlagKey(cacheKey);

		when(postRepository.findLikedPostIds(username)).thenReturn(new ArrayList<>());

		//when
		Set<Long> postIdSet = postCacheManager.getOrLoadPostsV3(username, cacheKey, dataLoaderFromDatabase);

		//then
		assertThat(postIdSet).isEmpty();

		Boolean emptyFlag = booleanValueRedisTemplate.opsForValue().get(emptyFlagKey);
		assertThat(emptyFlag).isTrue();
	}
}