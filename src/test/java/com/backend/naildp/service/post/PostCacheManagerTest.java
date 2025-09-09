package com.backend.naildp.service.post;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import com.backend.naildp.config.IntegrationTest;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.post.cache.PostCacheRepository;

@IntegrationTest
class PostCacheManagerTest {

	@Autowired
	private PostCacheManager postCacheManager;

	@Autowired
	private RedisTemplate<String, ?> redisTemplate;

	@SpyBean
	private PostCacheRepository postCacheRepository;

	@MockBean
	private PostRepository postRepository;

	@AfterEach
	void tearDown() {
		Set<String> keys = redisTemplate.keys("*");
		if(keys != null &&  !keys.isEmpty()) {
			redisTemplate.delete(keys);
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


}