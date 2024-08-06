package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.naildp.dto.archive.ArchiveRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchiveService {
	private final UserRepository userRepository;
	private final ArchiveRepository archiveRepository;

	public void createArchive(String nickname, ArchiveRequestDto archiveRequestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));
		Archive archive = Archive.builder()
			.user(user)
			.name(archiveRequestDto.getArchiveName())
			.build();

		archiveRepository.save(archive);
	}

	public void getArchives(String nickname) {

		List<Archive> archive = archiveRepository.findArchivesByUserNicknameOrderByCreatedDateDesc(nickname);

	}
}
