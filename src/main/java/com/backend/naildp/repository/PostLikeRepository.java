package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	@Query("select pl from PostLike pl join fetch pl.post p where pl.user.nickname = :nickname")
	List<PostLike> findAllByUserNickname(@Param("nickname") String nickname);

	@Query("delete from PostLike pl where pl.post.id = :postId")
	void deletePostLikeByPostId(@Param("postId") Long postId);
}
