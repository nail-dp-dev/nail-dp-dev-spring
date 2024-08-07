package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ArchivePost;

public interface ArchivePostRepository extends JpaRepository<ArchivePost, Long> {

	@Query("select ap from ArchivePost ap "
		+ "join fetch ap.archive a "
		+ "join fetch ap.post p "
		+ "where a.user.nickname = :nickname")
	List<ArchivePost> findAllByArchiveUserNickname(@Param("nickname") String nickname);

	@Query("select ap from ArchivePost ap join fetch ap.archive a join fetch ap.post p where a.user.nickname = :nickname and p.tempSave = false")
	List<ArchivePost> findAllArchivePostsByUserNicknameAndTempSaveIsFalse(@Param("nickname") String nickname);

	List<PostMapping> findArchivePostsByArchiveUserNickname(String nickname);
}
