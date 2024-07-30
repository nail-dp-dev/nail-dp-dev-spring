package com.backend.naildp.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TagRequestDto {
	@NotBlank(message = "태그를 입력해주세요")
	private String tagName;
}
