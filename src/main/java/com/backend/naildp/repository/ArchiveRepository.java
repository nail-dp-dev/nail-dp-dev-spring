package com.backend.naildp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.naildp.entity.Archive;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {
	int countArchivesByUserNickname(String nickname);

	Optional<Archive> findArchiveById(Long archiveId);
}
