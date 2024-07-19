package com.backend.naildp.dto.home;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.naildp.common.FileExtensionChecker;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HomePostResponse {

	private Long postId;
	private Long photoId;
	private String photoUrl;
	private Boolean isPhoto;
	private Boolean isVideo;
	private Boolean like;
	private Boolean saved;
	private LocalDateTime createdDate;

	public HomePostResponse(Post post, List<Post> savedPosts, List<Post> likedPosts) {
		Photo photo = post.getPhotos().get(0);

		postId = post.getId();
		photoId = photo.getId();
		photoUrl = photo.getPhotoUrl();
		isPhoto = FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl());
		isVideo = FileExtensionChecker.isVideoExtension(photo.getPhotoUrl());
		like = likedPosts.contains(post);
		saved = savedPosts.contains(post);
		createdDate = post.getCreatedDate();
	}

	public static HomePostResponse likedPostResponse(Post post, List<Post> savedPost) {
		Photo photo = post.getPhotos().get(0);

		return HomePostResponse.builder()
			.postId(post.getId())
			.photoId(photo.getId())
			.photoUrl(photo.getPhotoUrl())
			.isPhoto(FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl()))
			.isVideo(FileExtensionChecker.isVideoExtension(photo.getPhotoUrl()))
			.like(true)
			.saved(savedPost.contains(post))
			.createdDate(post.getCreatedDate())
			.build();
	}

	public static HomePostResponse recentPostForAnonymous(Post post) {
		Photo photo = post.getPhotos().get(0);

		return HomePostResponse.builder()
			.postId(post.getId())
			.photoId(photo.getId())
			.photoUrl(photo.getPhotoUrl())
			.isPhoto(FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl()))
			.isVideo(FileExtensionChecker.isVideoExtension(photo.getPhotoUrl()))
			.like(false)
			.saved(false)
			.createdDate(post.getCreatedDate())
			.build();
	}
}
