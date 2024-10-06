package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
	List<Photo> findAllByPostId(Long postId);

	List<Photo> findByPhotoUrlIn(List<String> deletedFileUrls);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from Photo p where p.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);
}
