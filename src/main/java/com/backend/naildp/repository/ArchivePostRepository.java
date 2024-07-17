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
}
// 1, 내가 팔로우한 사람의 포스트 boundary가 follow 인경우 카운트 + all 카운트
// 2. 카운트 하려는 포스트의 boundary가 all이거나 follow면서 그 사람의 팔로워에 내가 포함된 경우
