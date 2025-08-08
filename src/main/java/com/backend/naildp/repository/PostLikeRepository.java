package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	@Query("select pl from PostLike pl join fetch pl.post p where pl.user.nickname = :nickname")
	List<PostLike> findAllByUserNickname(@Param("nickname") String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	void deletePostLikeById(Long postLikeId);

	Optional<PostLike> findPostLikeByUserNicknameAndPostId(String nickname, Long postId);

	@Query("select pl from PostLike pl join fetch pl.post p "
		+ "where pl.user.nickname = :nickname and p.boundary <> :boundary and p.tempSave = false")
	Page<PostLike> findPagedPostLikesByBoundaryOpened(PageRequest pageRequest, @Param("nickname") String nickname,
		@Param("boundary") Boundary boundary);

	@Query("select pl from PostLike pl join fetch pl.post p"
		+ " where pl.user.nickname = :nickname"
		+ " and p.tempSave = false"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<PostLike> findPostLikesByFollowing(@Param("nickname") String nickname,
		@Param("following") List<User> following, PageRequest pageRequest);

	@Query("select pl from PostLike pl join fetch pl.post p"
		+ " where pl.user.nickname = :nickname"
		+ " and p.tempSave = false"
		+ " and pl.id < :id"
		+ " and (p.boundary = 'ALL' or (p.boundary = 'FOLLOW' and p.user in :following))")
	Slice<PostLike> findPostLikesByIdAndFollowing(@Param("nickname") String nickname, @Param("id") Long cursorId,
		@Param("following") List<User> following, PageRequest pageRequest);

	@Query("SELECT pl.post FROM PostLike pl WHERE pl.user.nickname = :nickname")
	List<Post> findPostLikesByUserNickname(@Param("nickname") String nickname);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PostLike pl where pl.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);

	@Query("select pl from PostLike pl join fetch pl.post p where pl.post.id = :postId and pl.user.id = :userId")
	Optional<PostLike> findPostLikeByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") UUID userId);

	boolean existsPostLikeByPostIdAndUserId(Long postId, UUID userId);

}
