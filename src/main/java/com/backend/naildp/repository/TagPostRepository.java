package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.TagPost;

public interface TagPostRepository extends JpaRepository<TagPost, Long> {
	void deleteAllByPostId(Long postId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("DELETE FROM TagPost tp WHERE tp.post.id = :postId")
	void deleteTagPostsByPostId(@Param("postId") Long postId);

}