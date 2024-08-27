package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.repository.ArchiveMapping;

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

	public static UserArchiveResponseDto userArchiveResponseDto(ArchiveMapping archive) {
		UserArchiveResponseDtoBuilder builder = UserArchiveResponseDto.builder()
			.archiveId(archive.getId())
			.boundary(archive.getBoundary())
			.postCount(archive.getPostCount())
			.archiveName(archive.getName());

		setArchiveImage(builder, archive);

		return builder.build();
	}

	public static UserArchiveResponseDto otherArchiveResponseDto(ArchiveMapping archive, Boolean isFollower) {
		UserArchiveResponseDtoBuilder builder = UserArchiveResponseDto.builder()
			.archiveId(archive.getId())
			.boundary(archive.getBoundary())
			.postCount(archive.getPostCount())
			.archiveName(archive.getName());

		setArchiveImage(builder, archive);
		setLockStatus(builder, archive, isFollower);

		return builder.build();
	}

	private static void setArchiveImage(UserArchiveResponseDtoBuilder builder,
		ArchiveMapping archive) {
		if (archive.getArchiveImgUrl() != null) {
			builder.archiveImgUrl(archive.getArchiveImgUrl())
				.isPhoto(FileExtensionChecker.isPhotoExtension(archive.getArchiveImgUrl()))
				.isVideo(FileExtensionChecker.isVideoExtension(archive.getArchiveImgUrl()));
		}
	}

	private static void setLockStatus(UserArchiveResponseDtoBuilder builder,
		ArchiveMapping archive, Boolean isFollower) {
		boolean isLocked;
		isLocked = (archive.getBoundary() != Boundary.FOLLOW || !isFollower) && archive.getBoundary() != Boundary.ALL;
		builder.isLock(isLocked);
	}
}
