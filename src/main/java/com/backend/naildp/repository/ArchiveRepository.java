package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

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
}

