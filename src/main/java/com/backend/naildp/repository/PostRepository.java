package com.backend.naildp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByBoundaryAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	@Query(value = "select p from Post p left join fetch p.photos ph where p.boundary = :boundary and p.tempSave = false",
		countQuery = "select count(p) from Post p where p.tempSave = false")
	Page<Post> findPostsAndPhotoByBoundaryAll(@Param("boundary") Boundary boundary, PageRequest pageRequest);

}
