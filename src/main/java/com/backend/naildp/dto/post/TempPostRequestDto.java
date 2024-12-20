package com.backend.naildp.dto.post;

import java.util.List;

import com.backend.naildp.common.Boundary;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TempPostRequestDto {
	private String postContent;
	private Boolean tempSave;
	private Boundary boundary;
	@Valid
	private List<TagRequestDto> tags;
	private List<String> deletedFileUrls;
}
