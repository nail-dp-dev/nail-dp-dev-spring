package com.backend.naildp.dto.archive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveResponseDto {
	private Long archiveId;
	private String boundary;
	private String archiveImageUrl;
	private String postCount;

	public static ArchiveResponseDto of (){
		return
	}

}
