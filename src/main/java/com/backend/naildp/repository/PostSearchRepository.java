package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.Post;

public interface PostSearchRepository {
	Slice<Post> searchPostByKeyword(Pageable pageable, List<String> keywords, String username, Long cursorId);

	List<Post> findPostsInArchive(String username);

	List<Post> findLikedPosts(String username);
}
