package com.backend.naildp.dto.userInfo;

import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TempSaveResponseDto {
	private Long postId;
	private Long photoId;
	private String photoUrl;
	private Boolean isPhoto;
	private Boolean isVideo;

	public TempSaveResponseDto(Post post) {
		if (!post.getPhotos().isEmpty()) {
			Photo photo = post.getPhotos().get(0);

			photoId = photo.getId();
			photoUrl = photo.getPhotoUrl();
			isPhoto = FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl());
			isVideo = FileExtensionChecker.isVideoExtension(photo.getPhotoUrl());
		}

		postId = post.getId();
	}
}
