package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	long countAllByPost(Post post);

	@Query("select c from Comment c join fetch c.user where c.id = :id")
	Optional<Comment> findCommentAndUser(@Param("id") Long commentId);

	@Query("select c from Comment c join fetch c.post join fetch c.user where c.id = :id")
	Optional<Comment> findCommentAndPostAndUser(@Param("id") Long id);

	@Query("select c from Comment c join fetch c.user where c.post.id = :postId")
	Slice<Comment> findCommentsByPostId(@Param("postId") Long postId, PageRequest pageRequest);

	@Query("select c from Comment c join fetch c.user where c.post.id = :postId"
		+ " and ((c.likeCount <= :likeCount and c.id < :commentId) or c.likeCount < :likeCount)")
	Slice<Comment> findCommentsByPostIdAndIdBefore(@Param("postId") Long postId,
		@Param("commentId") Long commentId,
		@Param("likeCount") Long likeCount,
		PageRequest pageRequest);

	@Query("select c.likeCount from Comment c where c.id = :commentId")
	long countLikesById(@Param("commentId") Long commentId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from Comment c where c.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);
}
