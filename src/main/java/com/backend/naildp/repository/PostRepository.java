package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {

	Slice<Post> findPostsByBoundaryAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	@Query("select p from Post p where p.tempSave = false"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<Post> findRecentPostsByFollowing(@Param("following") List<User> following, PageRequest pageRequest);

	@Query("select p from Post p where p.id < :id and p.tempSave = false"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<Post> findRecentPostsByIdAndFollowing(@Param("id") Long oldestPostId,
		@Param("following") List<User> following, PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryAndTempSaveFalse(Long id, Boundary boundary, PageRequest pageRequest);

	int countPostsByUserAndTempSaveIsFalse(User user);

	@Query("select p from Post p join fetch p.user u where p.id = :id and p.tempSave = false")
	Optional<Post> findPostAndWriterById(@Param("id") Long postId);

	@Query("select p from Post p join fetch p.user u where p.id = :id")
	Optional<Post> findPostAndUser(@Param("id") Long postId);

	Optional<Post> findPostByTempSaveIsTrueAndUser(User user);

	Optional<Post> findPostByTempSaveIsTrueAndUserNickname(String nickname);

	@Query("select p from Post p where p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname in :nickname)) "
		+ " and p.user.nickname =:nickname order by  p.createdDate desc ")
	Slice<Post> findUserPostsByFollow(@Param("nickname") String nickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("select p from Post p where p.id < :id and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname in :nickname)) "
		+ " and p.user.nickname =:nickname order by  p.createdDate desc ")
	Slice<Post> findUserPostsByIdAndFollow(@Param("id") Long id, @Param("nickname") String nickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("SELECT p FROM Post p JOIN p.postLikes pl WHERE pl.user.nickname = :nickname "
		+ "AND (p.boundary = 'ALL' "
		+ "OR (p.boundary = 'FOLLOW' AND p.user.nickname IN :followingNickname) "
		+ "OR (p.boundary = 'NONE' AND p.user.nickname = :nickname)) "
		+ "ORDER BY p.createdDate DESC")
	Slice<Post> findLikedUserPostsByFollow(@Param("nickname") String nickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("SELECT p FROM Post p JOIN p.postLikes pl WHERE p.id < :id AND pl.user.nickname = :nickname "
		+ "AND (p.boundary = 'ALL' "
		+ "OR (p.boundary = 'FOLLOW' AND p.user.nickname IN :followingNickname) "
		+ "OR (p.boundary = 'NONE' AND p.user.nickname = :nickname)) "
		+ "ORDER BY p.createdDate DESC")
	Slice<Post> findLikedUserPostsByIdAndFollow(@Param("id") Long id, @Param("nickname") String nickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);
}
