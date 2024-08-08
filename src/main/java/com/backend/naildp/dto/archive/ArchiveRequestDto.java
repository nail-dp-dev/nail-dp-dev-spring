package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveRequestDto {
	private String archiveName;
	private Boundary boundary;
}
