package com.backend.naildp.dto.post;

import com.backend.naildp.validation.ValidationGroups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TagRequestDto {
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "태그는 알파벳, 숫자, 한글만 포함될 수 있습니다.", groups = ValidationGroups.PatternCheckGroup.class)
	@NotBlank(message = "태그를 입력해주세요", groups = ValidationGroups.NotEmptyGroup.class)
	private String tagName;
}