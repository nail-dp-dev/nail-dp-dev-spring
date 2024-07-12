package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
	Optional<Tag> findByName(String name);
}
