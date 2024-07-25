package com.backend.naildp.dto.post;

import com.backend.naildp.entity.Photo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRequestDto {
	private String fileName;
	private Long fileSize;
	private String fileUrl;

	public FileRequestDto(Photo photo) {
		this.fileName = photo.getName();
		this.fileSize = photo.getSize();
		this.fileUrl = photo.getPhotoUrl();
	}
}
