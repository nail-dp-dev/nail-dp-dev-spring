package com.backend.naildp.service.post;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.post.cache.PostCacheRepository;
import com.backend.naildp.service.post.dto.PreferredPostDto;
import com.mongodb.lang.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReader {

	private static final String LIKED_POSTS_KEY_PREFIX = "likedPosts#";
	private static final String SAVED_POSTS_KEY_PREFIX = "savedPosts#";

	private final PostCacheRepository postCacheRepository;
	private final PostRepository postRepository;

	public List<Post> getLikedPosts(String username) {
		return postRepository.findLikedPosts(username);
	}

	public List<Post> getSavedPosts(String username) {
		return postRepository.findPostsInArchive(username);
	}

	@Nullable
	public Post findCursorPost(Long cursorPostId) {
		return cursorPostId == null ? null : postRepository.findById(cursorPostId).orElse(null);
	}

	public Set<Long> getSavedPostIdSet(String username) {
		String savedPostsKey = SAVED_POSTS_KEY_PREFIX + username;
		Set<Long> savedPostIdSet = postCacheRepository.findPostIdSet(savedPostsKey);

		if(Objects.isNull(savedPostIdSet)) {
			log.debug("SavedPost cache Miss for user:{}", username);

			// 캐시 갱신
			List<Long> savedPostIds = postRepository.findPostIdsInArchive(username);
			HashSet<Long> savedPostIdSetFromDatabase = new HashSet<>(savedPostIds);
			postCacheRepository.save(savedPostsKey, savedPostIdSetFromDatabase);

			return savedPostIdSetFromDatabase;
		}

		return savedPostIdSet;
	}

	public Set<Long> getLikedPostIds(String username) {
		String likedPostsKey = getLikedPostsKey(username);
		Set<Long> likedPostIdSet = postCacheRepository.findPostIdSet(likedPostsKey);

		if(Objects.isNull(likedPostIdSet)) {
			log.debug("LikedPost cache Miss for user:{}", username);

			// 캐시 갱신
			List<Long> likedPostIds = postRepository.findLikedPostIds(username);
			HashSet<Long> likedPostIdSetFromDatabase = new HashSet<>(likedPostIds);
			postCacheRepository.save(likedPostsKey, likedPostIdSetFromDatabase);

			return likedPostIdSetFromDatabase;
		}

		return likedPostIdSet;
	}

	public PreferredPostDto getPreferredPostIds(String username) {
		Set<Long> likedPostIds = getLikedPostIds(username);
		Set<Long> savedPostIds = getSavedPostIdSet(username);

		return PreferredPostDto.of(likedPostIds, savedPostIds);
	}

	@NotNull
	private static String getLikedPostsKey(String username) {
		return LIKED_POSTS_KEY_PREFIX + username;
	}
}
