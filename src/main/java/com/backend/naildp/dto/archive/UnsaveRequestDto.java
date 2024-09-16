package com.backend.naildp.dto.archive;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnsaveRequestDto {
	@NotNull(message = "postId 값이 없습니다")
	private Long postId;
	@NotEmpty(message = "archiveId 값이 없습니다")
	private List<Long> archiveId;
}
