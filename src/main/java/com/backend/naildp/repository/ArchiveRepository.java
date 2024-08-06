package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Archive;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

	List<Archive> findArchivesByUserNicknameOrderByCreatedDateDesc(String nickname);
}

