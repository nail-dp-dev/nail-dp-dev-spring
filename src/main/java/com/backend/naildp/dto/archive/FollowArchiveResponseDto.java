package com.backend.naildp.dto.archive;

import com.backend.naildp.common.FileExtensionChecker;
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
	private Boolean isPhoto;
	private Boolean isVideo;

	public static FollowArchiveResponseDto followArchiveResponseDto(ArchiveMapping archiveMapping) {
		if (archiveMapping.getArchiveImgUrl() != null) {
			return FollowArchiveResponseDto.builder()
				.archiveCount(archiveMapping.getArchiveCount())
				.nickname(archiveMapping.getNickname())
				.archiveImgUrl(archiveMapping.getArchiveImgUrl())
				.isPhoto(FileExtensionChecker.isPhotoExtension(archiveMapping.getArchiveImgUrl()))
				.isVideo(FileExtensionChecker.isVideoExtension(archiveMapping.getArchiveImgUrl()))
				.profileUrl(archiveMapping.getThumbnailUrl())
				.archiveId(archiveMapping.getId())
				.build();
		}
		return FollowArchiveResponseDto.builder()
			.archiveCount(archiveMapping.getArchiveCount())
			.nickname(archiveMapping.getNickname())
			.archiveImgUrl(archiveMapping.getArchiveImgUrl())
			.profileUrl(archiveMapping.getThumbnailUrl())
			.archiveId(archiveMapping.getId())
			.build();
	}
}
