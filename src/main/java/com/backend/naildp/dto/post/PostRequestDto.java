package com.backend.naildp.dto.post;

import java.util.List;

import com.backend.naildp.common.Boundary;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequestDto {
	private String postContent;
	private Boolean tempSave;
	private Boundary boundary;
	private List<TagRequestDto> tags;
}
