package com.backend.naildp.dto.archive;

import com.backend.naildp.validation.ValidationGroups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveNameRequestDto {
	@Size(max = 8, message = "이름은 8자 이하로 작성해주세요", groups = ValidationGroups.PatternCheckGroup.class)
	@NotBlank(message = "닉네임을 입력해주세요", groups = ValidationGroups.NotEmptyGroup.class)
	private String archiveName;
}
