package com.backend.naildp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

public interface PostSearchRepository {
	Slice<Post> searchPostByKeyword(Pageable pageable, List<String> keywords, String username, Long cursorId);

	List<Post> findPostsInArchive(String username);

	List<Post> findLikedPosts(String username);

	List<Long> findLikedPostIds(String username);

	List<Long> findPostIdsInArchive(String username);

	Slice<Post> findNewestPostSlice(String username, Long cursorPostId, Pageable pageable);

	Slice<Post> findTrendPostSlice(String username, Long cursorPostId, Pageable pageable);

	Slice<Post> findForYouPostSlice(String username, Long cursorPostId, List<Long> tagIdsInPosts, Pageable pageable);

	Slice<Post> findTrendPostSliceWithoutSubquery(String username, Post cursorPost, Pageable pageable);

	Slice<Post> findForYouPostSliceV2(String username, Post cursorPost, List<Long> tagIdsInPosts, Pageable pageable);

	Slice<Post> findForYouPostSliceV2WithoutTagPostJoin(String username, Post cursorPost, List<Long> tagIdsInPosts, Pageable pageable);

	Slice<Post> findForYouPostSliceV2WithoutTagPostJoinAndFollowJoin(String username, Post cursorPost, List<Long> tagIdsInPosts,
		List<User> readableUsers, Pageable pageable);

	Slice<Post> findForYouPostSliceV3(String username, Post cursorPost, List<Long> tagIdsInPosts, List<UUID> readableUserIds, Pageable pageable);
}
