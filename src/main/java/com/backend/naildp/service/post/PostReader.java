package com.backend.naildp.service.post;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.service.post.dto.PreferredPostDto;
import com.mongodb.lang.Nullable;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReader {

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

	public List<Long> getSavedPostIds(String username) {
		return postRepository.findPostIdsInArchive(username);
	}

	public List<Long> getLikedPostIds(String username) {
		return postRepository.findLikedPostIds(username);
	}

	public PreferredPostDto getPreferredPostIds(String username) {
		List<Long> likedPostIds = getLikedPostIds(username);
		List<Long> savedPostIds = getSavedPostIds(username);

		return PreferredPostDto.of(likedPostIds, savedPostIds);
	}
}
