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

public interface PostRepository extends JpaRepository<Post, Long>, PostSearchRepository {

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

	Optional<Post> findPostByTempSaveIsTrueAndUserNickname(String nickname);

	// 내가 올린 게시물 전체 조회
	@Query("select p from Post p where p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " and p.user.nickname =:postNickname order by  p.createdDate desc ")
	Slice<Post> findUserPostsByFollow(@Param("myNickname") String myNickname,
		@Param("postNickname") String postNickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("select p from Post p where p.id < :id and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " and p.user.nickname =:postNickname order by  p.createdDate desc ")
	Slice<Post> findUserPostsByIdAndFollow(@Param("id") Long id, @Param("myNickname") String myNickname,
		@Param("postNickname") String postNickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	// 내가 올린 게시물 중 좋아요 조회
	@Query("select p from Post p join p.postLikes pl where pl.user.nickname = :myNickname "
		+ "and (p.boundary = 'ALL' "
		+ "or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname) "
		+ "or (p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ "and p.user.nickname =:postNickname "
		+ "order by p.createdDate desc")
	Slice<Post> findLikedUserPostsByFollow(@Param("myNickname") String myNickname,
		@Param("postNickname") String postNickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("select p from Post p join p.postLikes pl where pl.user.nickname = :myNickname "
		+ "and p.id < :id "
		+ "and (p.boundary = 'ALL' "
		+ "or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname) "
		+ "or (p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ "and p.user.nickname =:postNickname "
		+ "order by p.createdDate desc")
	Slice<Post> findLikedUserPostsByIdAndFollow(@Param("id") Long id, @Param("myNickname") String myNickname,
		@Param("postNickname") String postNickname,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	// 아카이브 내 게시물 전체 조회
	@Query("select p from Post p join ArchivePost ap on p.id = ap.post.id"
		+ " where ap.archive.id = :archiveId"
		+ " and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " order by  p.createdDate desc ")
	Slice<Post> findArchivePostsByFollow(@Param("myNickname") String myNickname,
		@Param("archiveId") Long archiveId,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("select p from Post p join ArchivePost ap on p.id = ap.post.id"
		+ " where ap.archive.id = :archiveId"
		+ " and p.id < :id "
		+ " and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " order by  p.createdDate desc ")
	Slice<Post> findArchivePostsByIdAndFollow(@Param("id") Long id, @Param("myNickname") String myNickname,
		@Param("archiveId") Long archiveId,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	// 아카이브 내 게시물 좋아요 조회
	@Query("select p from Post p join ArchivePost ap on p.id = ap.post.id"
		+ " join PostLike pl on p.id = pl.post.id "
		+ " join Users u on pl.user.id = u.id"
		+ " where ap.archive.id = :archiveId"
		+ " and u.nickname = :myNickname"
		+ " and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " order by  p.createdDate desc ")
	Slice<Post> findLikedArchivePostsByFollow(@Param("myNickname") String myNickname,
		@Param("archiveId") Long archiveId,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query("select p from Post p join ArchivePost ap on p.id = ap.post.id"
		+ " join PostLike pl on p.id = pl.post.id "
		+ " where ap.archive.id = :archiveId"
		+ " and p.id < :id "
		+ " and p.tempSave = false"
		+ " and (p.boundary = 'ALL'"
		+ " or (p.boundary = 'FOLLOW' and p.user.nickname in :followingNickname)"
		+ " or(p.boundary = 'NONE' and p.user.nickname = :myNickname)) "
		+ " order by  p.createdDate desc ")
	Slice<Post> findLikedArchivePostsByIdAndFollow(@Param("id") Long id, @Param("myNickname") String myNickname,
		@Param("archiveId") Long archiveId,
		@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

}
