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
	@Query(
		"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
			+ "from Archive a left join a.archivePosts ap "
			+ "where a.user.nickname = :nickname "
			+ "group by a.id "
			+ "order by a.createdDate DESC")
	List<ArchiveMapping> findArchiveInfosByUserNickname(@Param("nickname") String nickname);

	int countArchivesByUserNickname(String nickname);

	Optional<Archive> findArchiveById(Long archiveId);

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
}
