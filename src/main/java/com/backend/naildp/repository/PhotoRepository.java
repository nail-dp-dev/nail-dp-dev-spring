package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
	Optional<Photo> findByPhotoUrl(String url);

	List<Photo> findAllByPostId(Long postId);
}
