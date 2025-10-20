package com.backend.naildp.repository.comment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.commentEntity.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

	Optional<CommentLike> findCommentLikeByCommentIdAndUserNickname(Long commentId, String nickname);

	Long countAllByCommentId(Long commentId);
}
