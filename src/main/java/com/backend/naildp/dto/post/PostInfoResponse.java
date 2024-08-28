package com.backend.naildp.dto.post;

import java.util.List;
import java.util.stream.Collectors;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
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
	private String boundary;
	private long likeCount;
	private boolean isLiked;
	private long commentCount;
	private long sharedCount;
	private List<String> tags;

	public static PostInfoResponse of(Post post, String userNickname, boolean followingStatus, int followerCount,
		List<Tag> tags) {

		User writer = post.getUser();
		List<PostLike> postLikes = post.getPostLikes();
		List<FileInfoResponse> fileInfoResponses = post.getPhotos().stream().map(FileInfoResponse::new).toList();

		return PostInfoResponse.builder()
			.nickname(writer.getNickname())
			.profileUrl(writer.getThumbnailUrl())
			.followingStatus(followingStatus)
			.followerCount(followerCount)
			.postContent(post.getPostContent())
			.boundary(post.getBoundary().toString())
			.likeCount(postLikes.size())
			.isLiked(postLikes.stream().anyMatch(postLike -> postLike.isLikedBy(userNickname)))
			.commentCount(post.getComments().size())
			.sharedCount(post.getSharing())
			.tags(tags.stream().map(Tag::getName).collect(Collectors.toList()))
			.files(fileInfoResponses)
			.build();
	}

	@Getter
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
