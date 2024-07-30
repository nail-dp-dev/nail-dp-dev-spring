package com.backend.naildp.dto.post;

import java.util.List;
import java.util.stream.Collectors;

import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Profile;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.User;

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

	public static PostInfoResponse of(Post post, User user, String profileUrl, boolean followingStatus, int followerCount,
		List<Tag> tags) {

		List<FileInfoResponse> fileInfoResponses = post.getPhotos().stream().map(FileInfoResponse::new).toList();

		return PostInfoResponse.builder()
			.nickname(user.getNickname())
			.profileUrl(profileUrl)
			.followingStatus(followingStatus)
			.postContent(post.getPostContent())
			.likeCount(post.getPostLikes().size())
			.commentCount(post.getComments().size())
			.tags(tags.stream().map(Tag::getName).collect(Collectors.toList()))
			.files(fileInfoResponses)
			.build();
	}

	@AllArgsConstructor
	@NoArgsConstructor
	public static class FileInfoResponse {
		private String fileUrl;
		private boolean isPhoto;
		private boolean isVideo;

		public FileInfoResponse(Photo photo) {
			this.fileUrl = photo.getPhotoUrl();
			this.isPhoto = FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl());
			this.isVideo = FileExtensionChecker.isVideoExtension(photo.getPhotoUrl());
		}
	}
}
