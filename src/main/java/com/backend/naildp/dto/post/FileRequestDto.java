package com.backend.naildp.dto.post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileRequestDto {
	private String fileName;
	private Long fileSize;
	private String fileUrl;
}
