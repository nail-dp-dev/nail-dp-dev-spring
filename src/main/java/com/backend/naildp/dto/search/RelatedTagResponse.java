package com.backend.naildp.dto.search;

import java.util.List;

import org.springframework.util.StringUtils;

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

	public RelatedTagResponse(Tag tag, String photoUrl) {
		this.tagName = tag.getName();
		this.tagImageUrl = photoUrl;
		this.isPhoto = true;
		this.isVideo = true;
	}
}
