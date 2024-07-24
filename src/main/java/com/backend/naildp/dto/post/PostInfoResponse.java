package com.backend.naildp.dto.post;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostInfoResponse {

	private String nickname;
	private String profileUrl;
	private boolean followingStatus;
	private long followerCount;
	private List<FileInfoResponse> files;
	private String postContent;
	private long likeCount;
	private long commentCount;
	private long sharedCount;
	private List<String> tags;

	@AllArgsConstructor
	@NoArgsConstructor
	public static class FileInfoResponse {
		private String fileUrl;
		private boolean isPhoto;
		private boolean isVideo;
	}
}
