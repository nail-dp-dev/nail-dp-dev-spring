package com.backend.naildp.service.tag;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.repository.tag.TagPostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagReader {

	private final TagPostRepository tagPostRepository;

	public List<Long> getTagIdsInPosts(List<Post> selectedPosts) {
		return tagPostRepository.findTagIdsInPosts(selectedPosts);
	}

	public List<Long> getTagIdsInPostIds(Set<Long> postIds) {
		if(postIds.isEmpty()) {
			return Collections.emptyList();
		}

		return tagPostRepository.findTagIdsInPostIds(postIds);
	}
}
