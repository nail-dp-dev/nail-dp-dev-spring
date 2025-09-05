package com.backend.naildp.service.post;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.post.cache.PostCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCacheManager {

	private final PostCacheRepository postCacheRepository;
	private final PostRepository postRepository;

	public Set<Long> getOrLoadSavedPostIds(String username) {
		String savedPostsKey = CacheKeyGenerator.getSavedPostsKey(username);
		return getOrLoadPosts(username, savedPostsKey, () -> postRepository.findPostIdsInArchive(username));
	}

	public Set<Long> getOrLoadLikedPostIds(String username) {
		String likedPostsKey = CacheKeyGenerator.getLikedPostsKey(username);
		return getOrLoadPosts(username, likedPostsKey, () -> postRepository.findLikedPostIds(username));
	}

	public Set<Long> getOrLoadPosts(String username, String cacheKey, Supplier<List<Long>> dataLoaderFromDatabase) {
		Set<Long> postIdSet = postCacheRepository.findPostIdSet(cacheKey);

		// 캐시 없을 때 db에서 로드 후 반환
		if(Objects.isNull(postIdSet)|| postIdSet.isEmpty()) {
			log.debug("postIdSet cache miss for key:{}, username:{}", cacheKey, username);

			List<Long> postIds = dataLoaderFromDatabase.get();
			Set<Long> postIdSetFromDatabase = new HashSet<>(postIds);
			postCacheRepository.save(cacheKey, postIdSetFromDatabase);

			return postIdSetFromDatabase;
		}

		// 캐시 존재시 그대로 반환
		return postIdSet;
	}

}
