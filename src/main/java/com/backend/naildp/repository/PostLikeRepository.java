package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	@Query("select pl from PostLike pl join fetch pl.post p where pl.user.nickname = :nickname")
	List<PostLike> findAllByUserNickname(@Param("nickname") String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	void deletePostLikeById(Long postLikeId);

	Optional<PostLike> findPostLikeByUserNicknameAndPostId(String nickname, Long postId);

	@Query("select pl from PostLike pl join fetch pl.post p "
		+ "where pl.user.nickname = :nickname and p.boundary <> :boundary and p.tempSave = false")
	Page<PostLike> findPostLikesByUserNickname(PageRequest pageRequest, @Param("nickname") String nickname,
		@Param("boundary") Boundary boundary);
}
