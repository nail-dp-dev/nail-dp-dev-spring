package com.backend.naildp.dto.post;

import java.util.List;

import com.backend.naildp.common.Boundary;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EditPostResponseDto {
	private String postContent;
	private List<String> tags;
	private Boolean tempSave;
	private List<FileRequestDto> photos;
	private Boundary boundary;
}
