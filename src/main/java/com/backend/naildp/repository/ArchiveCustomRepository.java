package com.backend.naildp.repository;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.backend.naildp.dto.archive.FollowArchiveResponseDto;
import com.backend.naildp.dto.archive.UserArchiveResponseDto;

public interface ArchiveCustomRepository {
	Slice<UserArchiveResponseDto> findUserArchives(String nickname, Long cursorId, int size);

	Slice<FollowArchiveResponseDto> findFollowingArchives(List<String> followingNicknames, Long cursorId, int size);
}