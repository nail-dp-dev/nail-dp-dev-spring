package com.backend.naildp.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {

	Slice<Post> findPostsByBoundaryAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByBoundaryNotAndTempSaveFalse(Boundary boundary, PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(Long id, Boundary boundary,
		PageRequest pageRequest);

	Slice<Post> findPostsByIdBeforeAndBoundaryAndTempSaveFalse(Long id, Boundary boundary, PageRequest pageRequest);

	int countPostsByUserAndTempSaveIsFalse(User user);

}
