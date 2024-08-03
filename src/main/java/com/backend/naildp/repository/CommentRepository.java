package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	long countAllByPost(Post post);
}
