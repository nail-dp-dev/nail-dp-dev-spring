package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
	Optional<Photo> findByPhotoUrl(String url);

	List<Photo> findAllByPostId(Long postId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("DELETE FROM Photo p WHERE p.post.id = :postId")
	void deletePhotosByPostId(@Param("postId") Long postId);

}
