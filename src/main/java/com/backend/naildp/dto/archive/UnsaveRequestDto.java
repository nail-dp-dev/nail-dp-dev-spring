package com.backend.naildp.dto.archive;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnsaveRequestDto {
	private Long postId;
	private List<Long> archiveId;
}
