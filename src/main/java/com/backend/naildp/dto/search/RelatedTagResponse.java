package com.backend.naildp.dto.search;

import java.util.List;

import org.springframework.util.StringUtils;

import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RelatedTagResponse {

	private String tagName;
	private String tagImageUrl;
	private boolean isPhoto;
	private boolean isVideo;

	public RelatedTagResponse(Tag tag, List<Photo> photos) {
		String photoUrl = !photos.isEmpty() ? photos.get(0).getPhotoUrl() : "default.jpg";
		this.tagName = tag.getName();
		this.tagImageUrl = photoUrl;
		this.isPhoto = FileExtensionChecker.isPhotoExtension(photoUrl);
		this.isVideo = FileExtensionChecker.isVideoExtension(photoUrl);
	}
}
