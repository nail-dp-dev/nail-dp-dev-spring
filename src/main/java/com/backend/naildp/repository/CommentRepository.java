package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	long countAllByPost(Post post);

	@Query("select c from Comment c join fetch c.post join fetch c.user where c.id = :id")
	Optional<Comment> findCommentAndPostAndUser(@Param("id") Long id);
}
