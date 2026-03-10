package com.backend.naildp.repository.comment;

import com.backend.naildp.entity.commentEntity.Comment;
import com.backend.naildp.entity.userEntity.User;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.commentEntity.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

	Optional<CommentLike> findCommentLikeByCommentIdAndUserNickname(Long commentId, String nickname);

	Long countAllByCommentId(Long commentId);

    boolean existsByCommentAndUser(Comment comment, User user);
}
