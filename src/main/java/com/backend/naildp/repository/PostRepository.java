package com.backend.naildp.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query(value = "select p from Post p where p.boundary = :boundary and p.tempSave = false",
		countQuery = "select count(p) from Post p where p.tempSave = false")
	Slice<Post> findPostsAndPhotoByBoundaryAll(@Param("boundary") Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByBoundaryAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByBoundaryNotAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(Long id, Boundary boundary,
		PageRequest pageRequest);

	int countPostsByUserAndTempSaveIsFalse(User user);

}
