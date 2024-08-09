package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.ArchiveRequestDto;
import com.backend.naildp.dto.archive.ArchiveResponseDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchiveMapping;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchiveService {
	private final UserRepository userRepository;
	private final ArchiveRepository archiveRepository;
	private final ArchivePostRepository archivePostRepository;

	@Transactional
	public void createArchive(String nickname, ArchiveRequestDto archiveRequestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));

		if (user.getRole() == UserRole.USER) {
			int archiveCount = archiveRepository.countArchivesByUserNickname(nickname);
			if (archiveCount >= 4) {
				throw new CustomException("더이상 아카이브를 생성할 수 없습니다.", ErrorCode.INVALID_FORM);
			}
		}

		Archive archive = new Archive(user, archiveRequestDto.getArchiveName(), archiveRequestDto.getBoundary());

		archiveRepository.save(archive);

	}

	@Transactional(readOnly = true)
	public ArchiveResponseDto getArchives(String nickname) {

		List<ArchiveMapping> archives = archiveRepository.findArchiveInfosByUserNickname(nickname);

		return ArchiveResponseDto.of(archives);
	}

	public void saveArchive(String nickname, Long postId, ArchiveIdRequestDto requestDto) {

	}
}
