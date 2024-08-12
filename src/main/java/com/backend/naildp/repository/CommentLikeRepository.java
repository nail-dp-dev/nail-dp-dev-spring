package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.backend.naildp.entity.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	void deleteByCommentIdAndUserNickname(Long commentId, String nickname);
}
