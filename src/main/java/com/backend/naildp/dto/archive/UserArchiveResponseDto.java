package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.FileExtensionChecker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserArchiveResponseDto {
	private Long archiveId;
	private String archiveName;
	private Boundary boundary;
	private String archiveImgUrl;
	private Long postCount;
	private Boolean isPhoto;
	private Boolean isVideo;
	private Boolean isLock;

	public UserArchiveResponseDto(Long archiveId, String archiveName, Boundary boundary, String archiveImgUrl,
		Long postCount, Boolean isFollower) {
		this.archiveId = archiveId;
		this.archiveName = archiveName;
		this.boundary = boundary;
		this.archiveImgUrl = archiveImgUrl;
		this.postCount = postCount;
		this.isPhoto = FileExtensionChecker.isPhotoExtension(archiveImgUrl);
		this.isVideo = FileExtensionChecker.isVideoExtension(archiveImgUrl);
		this.isLock = calculateIsLock(boundary, isFollower);
	}

	private Boolean calculateIsLock(Boundary boundary, Boolean isFollower) {
		return (boundary != Boundary.FOLLOW || !isFollower) && boundary != Boundary.ALL;
	}

	public static UserArchiveResponseDto otherArchiveResponseDto(UserArchiveResponseDto dto, Boolean isFollower) {
		return UserArchiveResponseDto.builder()
			.archiveId(dto.getArchiveId())
			.archiveName(dto.getArchiveName())
			.boundary(dto.getBoundary())
			.archiveImgUrl(dto.getArchiveImgUrl())
			.postCount(dto.getPostCount())
			.isPhoto(dto.getIsPhoto())
			.isVideo(dto.getIsVideo())
			.isLock((dto.getBoundary() != Boundary.FOLLOW || !isFollower) && dto.getBoundary() != Boundary.ALL)
			.build();
	}

}
