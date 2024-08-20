package com.backend.naildp.dto.archive;

import java.util.List;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.repository.ArchiveMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveResponseDto {
	private List<ArchiveInfoResponse> archiveList;

	public static ArchiveResponseDto of(List<ArchiveMapping> archives) {
		List<ArchiveInfoResponse> archiveInfoResponses = archives.stream().map(ArchiveInfoResponse::new).toList();

		return ArchiveResponseDto.builder()
			.archiveList(archiveInfoResponses)
			.build();
	}

	@Slf4j(topic = "archiveUrl 확인")
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ArchiveInfoResponse {
		private Long archiveId;
		private String archiveName;
		private Boundary boundary;
		private String archiveImgUrl;
		private Long postCount;
		private Boolean isPhoto;
		private Boolean isVideo;

		public ArchiveInfoResponse(ArchiveMapping archive) {
			this.archiveId = archive.getId();
			this.boundary = archive.getBoundary();
			this.postCount = archive.getPostCount();
			this.archiveName = archive.getName();
			if (archive.getArchiveImgUrl() != null) {
				log.info(getArchiveImgUrl());
				this.archiveImgUrl = archive.getArchiveImgUrl();
				this.isPhoto = FileExtensionChecker.isPhotoExtension(archive.getArchiveImgUrl());
				this.isVideo = FileExtensionChecker.isVideoExtension(archive.getArchiveImgUrl());
			}
		}
	}
}
