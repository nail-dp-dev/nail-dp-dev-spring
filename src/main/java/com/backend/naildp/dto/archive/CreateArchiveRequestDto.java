package com.backend.naildp.dto.archive;

import com.backend.naildp.common.Boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateArchiveRequestDto {
	private String archiveName;
	private Boundary boundary;
}
