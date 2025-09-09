package com.backend.naildp.service.post;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.post.cache.EmptyFlagCacheRepository;
import com.backend.naildp.repository.post.cache.PostCacheRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCacheManager {

	private final PostCacheRepository postCacheRepository;
	private final EmptyFlagCacheRepository emptyFlagCacheRepository;
	private final PostRepository postRepository;

	public Set<Long> getOrLoadSavedPostIds(String username) {
		String savedPostsKey = CacheKeyGenerator.getSavedPostsKey(username);
		return getOrLoadPostsV3(username, savedPostsKey, () -> postRepository.findPostIdsInArchive(username));
	}

	public Set<Long> getOrLoadLikedPostIds(String username) {
		String likedPostsKey = CacheKeyGenerator.getLikedPostsKey(username);
		return getOrLoadPostsV3(username, likedPostsKey, () -> postRepository.findLikedPostIds(username));
	}

	public Set<Long> getOrLoadPosts(String username, String cacheKey, Supplier<List<Long>> dataLoaderFromDatabase) {
		Set<Long> postIdSet = getPostIdSetFromCache(cacheKey);

		// 캐시 없을 때 db에서 로드 후 반환
		if(Objects.isNull(postIdSet)) {
			log.debug("postIdSet cache miss for key:{}, username:{}", cacheKey, username);

			List<Long> postIds = dataLoaderFromDatabase.get();
			Set<Long> postIdSetFromDatabase = new HashSet<>(postIds);
			postCacheRepository.save(cacheKey, postIdSetFromDatabase);

			return postIdSetFromDatabase;
		}

		// 캐시 존재시 그대로 반환
		return postIdSet;
	}

	// 사용자의 캐시값이 비어있는지 나타내는 emptyFlagCache 를 확인
	// 해당 emptyFlag가 없다면 db 에서 값이 있는지 확인한후
	// 빈 리스트라면 emptyFlag를 true로 설정, 빈 리스트가 아니라면 emptyFlag를 false로 설정하고 캐시값에 저장 및 반환
	// 해당 emptyFlag 가 있다면 값에 따라 진행
	// false 라면 실제 데이터 캐시를 찾아서 반환, true 라면 그냥 빈 리스트를 반환
	public Set<Long> getOrLoadPostsV2(String username, String cacheKey, Supplier<List<Long>> dataLoaderFromDatabase) {
		String emptyFlagKey = cacheKey + ":empty";

		Optional<Boolean> optionalFlag = emptyFlagCacheRepository.getFlag(emptyFlagKey);

		// empty 캐시 값이 없으므로
		// db 에서 값을 조회후 빈리스트면 emptyFlag를 true로 설정,
		// 빈리스트가 아니라면 emptyFlag를 False로 설정하고 캐시값에 해당 postId 넣기
		if(optionalFlag.isEmpty()) {
			List<Long> postIds = dataLoaderFromDatabase.get();

			if(postIds.isEmpty()) {
				emptyFlagCacheRepository.saveFlag(emptyFlagKey, true);
				return new HashSet<>();
			}

			emptyFlagCacheRepository.saveFlag(emptyFlagKey, false);

			Set<Long> postIdSetFromDatabase = new HashSet<>(postIds);
			postCacheRepository.save(cacheKey, postIdSetFromDatabase);

			return postIdSetFromDatabase;
		}

		// empty 캐시 값이 있으므로
		// flag == false 면 메인 캐시에서 값을 찾아 반환
		// flag == true 면 빈 셋 반환
		Boolean emptyFlag = optionalFlag.get();
		if(emptyFlag) {
			return new HashSet<>();
		} else {
			return postCacheRepository.findPostIdSet(cacheKey);
		}
	}

	// 사용자의 캐시값이 비어있는지 나타내는 emptyFlagCache 를 확인
	// 해당 emptyFlag가 없다면 db 에서 값이 있는지 확인한후
	// 빈 리스트라면 emptyFlag를 true로 설정, 빈 리스트가 아니라면 emptyFlag를 false로 설정하고 캐시값에 저장 및 반환
	// 해당 emptyFlag 가 있다면 값에 따라 진행
	// false 라면 실제 데이터 캐시를 찾아서 반환, true 라면 그냥 빈 리스트를 반환
	public Set<Long> getOrLoadPostsV3(String username, String cacheKey, Supplier<List<Long>> dataLoaderFromDatabase) {
		String emptyFlagKey = CacheKeyGenerator.generateEmptyFlagKey(cacheKey);

		Boolean emptyFlag = emptyFlagCacheRepository.getFlag(emptyFlagKey)
			.orElseGet(() -> {
				log.info("emptyFlag is null for key:{}, username:{}", emptyFlagKey, username);

				List<Long> postIds = dataLoaderFromDatabase.get();

				if (postIds.isEmpty()) {
					log.info("db returns empty postId list for username:{}", username);
					emptyFlagCacheRepository.saveTrue(emptyFlagKey);
					return true;
				}

				emptyFlagCacheRepository.saveFalse(emptyFlagKey);
				updatePostIdCache(cacheKey, postIds);
				log.info("caching postIds for cacheKey:{}, username:{}", cacheKey, username);

				return false;
			});

		return emptyFlag ? new HashSet<>() : postCacheRepository.findPostIdSet(cacheKey);
	}

	@Nullable
	private Set<Long> getPostIdSetFromCache(String cacheKey) {
		return postCacheRepository.hasKey(cacheKey) ? postCacheRepository.findPostIdSet(cacheKey) : null;
	}

	private void updatePostIdCache(String cacheKey, List<Long> postIds) {
		Set<Long> postIdSetFromDatabase = new HashSet<>(postIds);
		postCacheRepository.save(cacheKey, postIdSetFromDatabase);
	}

}
