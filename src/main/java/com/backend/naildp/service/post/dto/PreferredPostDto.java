package com.backend.naildp.service.post.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.backend.naildp.entity.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PreferredPostDto {

	private Set<Long> likedPostIds;
	private Set<Long> savedPostIds;

	public PreferredPostDto(Set<Long> likedPostIds, Set<Long> savedPostIds) {
		this.likedPostIds = likedPostIds;
		this.savedPostIds = savedPostIds;
	}

	public static PreferredPostDto of(List<Long> likedPostIds, List<Long> savedPostIds) {
		Set<Long> likedPostIdSet = new HashSet<>(likedPostIds);
		Set<Long> savedPostIdSet = new HashSet<>(savedPostIds);
		return new PreferredPostDto(likedPostIdSet, savedPostIdSet);
	}

	public Set<Long> all() {
		HashSet<Long> postIds = new HashSet<>(likedPostIds);
		postIds.addAll(savedPostIds);
		return postIds;
	}

	public boolean isLiked(Post post) {
		return likedPostIds.contains(post.getId());
	}

	public boolean isSaved(Post post) {
		return savedPostIds.contains(post.getId());
	}
}
