package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveBoundaryRequestDto {
	@NotNull(message = "공개범위를 입력해주세요")
	private Boundary boundary;
}
