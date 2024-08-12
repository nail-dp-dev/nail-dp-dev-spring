package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
}
