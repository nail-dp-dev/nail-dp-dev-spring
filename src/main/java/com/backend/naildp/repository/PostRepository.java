package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {

	Slice<Post> findPostsByBoundaryAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByBoundaryNotAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	@Query("select p from Post p where p.tempSave = false"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<Post> findRecentPostsByFollowing(@Param("following") List<User> following, PageRequest pageRequest);

	@Query("select p from Post p where p.id < :id and p.tempSave = false"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<Post> findRecentPostsByIdAndFollowing(@Param("id") Long oldestPostId,
		@Param("following") List<User> following, PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(Long id, Boundary boundary,
		PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryAndTempSaveFalse(Long id, Boundary boundary, PageRequest pageRequest);

	int countPostsByUserAndTempSaveIsFalse(User user);

	// @Query("select p from Post p join fetch p.photos where p.id=:postId")
	// Optional<Post> findPostAndPhotosById(@Param("postId") Long postId);
	//
	// @Query("select p from Post p join fetch p.tagPosts tp join fetch tp.tag where p.id = :postId")
	// Optional<Post> findPostAndTagPostsById(@Param("postId") Long postId);

}
