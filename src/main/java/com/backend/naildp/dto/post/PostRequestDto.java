package com.backend.naildp.dto.post;

import java.util.List;

import com.backend.naildp.common.Boundary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
	private String postContent;
	@NotNull(message = "임시저장 여부를 입력해주세요")
	private Boolean tempSave;
	@NotNull(message = "공개범위를 입력해주세요")
	private Boundary boundary;
	@NotEmpty(message = "태그를 추가해주세요")
	@Valid
	private List<TagRequestDto> tags;
	private List<String> deletedFileUrls;
}
