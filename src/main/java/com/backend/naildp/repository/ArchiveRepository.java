package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Archive;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {
	// 내 아카이브 조회
	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findArchiveInfosByUserNickname(@Param("nickname") String nickname, PageRequest pageRequest);

	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "and a.id < :id "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findArchiveInfosByIdAndUserNickname(@Param("nickname") String nickname, @Param("id") Long id,
		PageRequest pageRequest);

	// 다른 유저 아카이브 조회
	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "and a.boundary <> 'NONE'"
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findArchiveInfosWithoutNone(@Param("nickname") String nickname, PageRequest pageRequest);

	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "and a.boundary <> 'NONE'"
			+ "and a.id < :id "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findArchiveInfosByIdWithoutNone(@Param("nickname") String nickname, @Param("id") Long id,
		PageRequest pageRequest);

	int countArchivesByUserNickname(String nickname);

	Optional<Archive> findArchiveById(Long archiveId);

	// 팔로잉한 아카이브 하나씩 조회
	@Query(
		"select a.id as id, u.nickname as nickname, u.thumbnailUrl as thumbnailUrl, a.archiveImgUrl as archiveImgUrl, u.archiveCount as archiveCount "
			+ "from Archive a " + "join a.user u "
			+ "where u.nickname in :followingNickname "
			+ "and a.createdDate = (select MAX(a2.createdDate) from Archive a2 where a2.user = u and a2.boundary <> 'NONE') "
			+ "order by a.createdDate desc")
	Slice<ArchiveMapping> findArchivesByFollowing(@Param("followingNickname") List<String> followingNickname,
		PageRequest pageRequest);

	@Query(
		"select a.id as id, u.nickname as nickname, u.thumbnailUrl as thumbnailUrl, a.archiveImgUrl as archiveImgUrl, u.archiveCount as archiveCount "
			+ "from Archive a " + "join a.user u "
			+ "where u.nickname in :followingNickname "
			+ "and a.id < :id "
			+ "and a.createdDate = (select MAX(a2.createdDate) from Archive a2 where a2.user = u and a2.boundary <> 'NONE') "
			+ "order by a.createdDate desc")
	Slice<ArchiveMapping> findArchivesByIdAndFollowing(@Param("followingNickname") List<String> followingNickname,
		@Param("id") Long id,
		PageRequest pageRequest);

	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "and ap.post.id = :postId "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findSavedArchiveByPage(@Param("nickname") String nickname, @Param("postId") Long postId,
		PageRequest pageRequest);

	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "and ap.post.id = :postId "
			+ "and a.id < :id "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	Slice<ArchiveMapping> findSavedArchiveByIdAndPage(@Param("nickname") String nickname, @Param("postId") Long postId,
		@Param("id") Long cursorId,
		PageRequest pageRequest);
}
