package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Post;

public interface ArchivePostRepository extends JpaRepository<ArchivePost, Long> {

	@Query("select ap from ArchivePost ap "
		+ "join fetch ap.archive a "
		+ "join fetch ap.post p "
		+ "where a.user.nickname = :nickname")
	List<ArchivePost> findAllByArchiveUserNickname(@Param("nickname") String nickname);

	@Query("select ap from ArchivePost ap join fetch ap.archive a join fetch ap.post p where a.user.nickname = :nickname and p.tempSave = false")
	List<ArchivePost> findAllArchivePostsByUserNicknameAndTempSaveIsFalse(@Param("nickname") String nickname);

	@Query("select ap.post as post from ArchivePost ap join ap.post p where ap.archive.user.nickname = :nickname and p.tempSave = false")
	List<Post> findArchivePostsByArchiveUserNickname(@Param("nickname") String nickname);

	boolean existsByArchiveIdAndPostId(Long archiveId, Long postId);

	List<PostMapping> findArchivePostsByArchiveId(Long archiveId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from ArchivePost ap where ap.archive.id = :archiveId")
	void deleteAllByArchiveId(@Param("archiveId") Long archiveId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from ArchivePost ap where ap.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);
}
