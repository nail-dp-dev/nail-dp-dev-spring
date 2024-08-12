package com.backend.naildp.dto.archive;

import com.backend.naildp.repository.ArchiveMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowArchiveResponseDto {
	private Long archiveId;
	private Long archiveCount;
	private String nickname;
	private String archiveImgUrl;
	private String profileUrl;

	public static FollowArchiveResponseDto followArchiveResponseDto(ArchiveMapping archiveMapping) {
		return FollowArchiveResponseDto.builder()
			.archiveCount(archiveMapping.getArchiveCount())
			.nickname(archiveMapping.getNickname())
			.archiveImgUrl(archiveMapping.getArchiveImgUrl())
			.profileUrl(archiveMapping.getThumbnailUrl())
			.archiveId(archiveMapping.getId())
			.build();
	}
}
