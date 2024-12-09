package com.backend.naildp.dto.archive;

import com.backend.naildp.common.FileExtensionChecker;

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
	private int archiveCount;
	private String nickname;
	private String archiveImgUrl;
	private String profileUrl;
	private Boolean isPhoto;
	private Boolean isVideo;

	public FollowArchiveResponseDto(Long archiveId, int archiveCount, String nickname, String archiveImgUrl,
		String profileUrl) {
		this.archiveId = archiveId;
		this.archiveCount = archiveCount;
		this.nickname = nickname;
		this.archiveImgUrl = archiveImgUrl;
		this.profileUrl = profileUrl;
		this.isPhoto = FileExtensionChecker.isPhotoExtension(archiveImgUrl);
		this.isVideo = FileExtensionChecker.isVideoExtension(archiveImgUrl);
	}
}
