package com.backend.naildp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.TagPost;

public interface TagPostRepository extends JpaRepository<TagPost, Long> {
}