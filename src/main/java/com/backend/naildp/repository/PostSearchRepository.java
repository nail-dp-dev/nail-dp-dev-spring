package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.Post;

public interface PostSearchRepository {
	Slice<Post> searchPostByKeyword(Pageable pageable, List<String> keywords, String username, Long cursorId);

	List<Post> findPostsInArchive(String username);

	List<Post> findLikedPosts(String username);

	Slice<Post> findNewestPostSlice(String username, Long cursorPostId, Pageable pageable);

	Slice<Post> findTrendPostSlice(String username, Long cursorPostId, Pageable pageable);

	Slice<Post> findTrendPostSliceUsingLeftJoin(String username, Long cursorPostId, Pageable pageable);

	Slice<Post> findForYouPostSlice(String username, Long cursorPostId, List<Long> tagIdsInPosts, Pageable pageable);
}
