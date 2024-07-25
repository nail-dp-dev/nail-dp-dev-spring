package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
	List<Photo> findAllByPostId(Long postId);

	List<Photo> findByPhotoUrlIn(List<String> deletedFileUrls);
}
