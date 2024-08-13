package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateArchiveRequestDto {
	private String archiveName;
	private Boundary boundary;
}
