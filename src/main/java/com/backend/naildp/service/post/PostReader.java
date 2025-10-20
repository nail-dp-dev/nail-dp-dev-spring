package com.backend.naildp.service.post;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.service.post.dto.PreferredPostDto;
import com.mongodb.lang.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReader {

	private final PostRepository postRepository;
	private final PostCacheManager postCacheManager;

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

	public PreferredPostDto getPreferredPostIds(String username) {
		Set<Long> likedPostIds = findLikedPostIdSet(username);
		Set<Long> savedPostIds = findSavedPostIdSet(username);
		// Set<Long> likedPostIds = new HashSet<>(postRepository.findLikedPostIds(username));
		// Set<Long> savedPostIds = new HashSet<>(postRepository.findPostIdsInArchive(username));

		return PreferredPostDto.of(likedPostIds, savedPostIds);
	}

	public Set<Long> findLikedPostIdSet(String username) {
		return postCacheManager.getOrLoadLikedPostIds(username);
	}

	public Set<Long> findSavedPostIdSet(String username) {
		return postCacheManager.getOrLoadSavedPostIds(username);
	}

}
