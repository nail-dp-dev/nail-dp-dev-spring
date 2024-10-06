package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.validation.ValidationGroups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateArchiveRequestDto {
	@Size(max = 8, message = "이름은 8자 이하로 작성해주세요", groups = ValidationGroups.PatternCheckGroup.class)
	@NotBlank(message = "닉네임을 입력해주세요", groups = ValidationGroups.NotEmptyGroup.class)
	private String archiveName;
	@NotNull(message = "공개범위를 입력해주세요")
	private Boundary boundary;
}
