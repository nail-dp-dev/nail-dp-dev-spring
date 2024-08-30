package com.backend.naildp.dto.archive;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.naildp.common.Boundary;
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
public class ArchivePostResponseDto {
	private Long postId;
	private Long photoId;
	private String photoUrl;
	private Boolean isPhoto;
	private Boolean isVideo;
	private Boolean like;
	private Boolean saved;
	private LocalDateTime createdDate;
	private Boundary boundary;
	private String archiveName;

	public static ArchivePostResponseDto archivePostInfo(Post post, List<Post> savedPost, List<Post> likedPost,
		String name) {
		Photo photo = post.getPhotos().get(0);

		return ArchivePostResponseDto.builder()
			.postId(post.getId())
			.photoId(photo.getId())
			.photoUrl(photo.getPhotoUrl())
			.isPhoto(FileExtensionChecker.isPhotoExtension(photo.getPhotoUrl()))
			.isVideo(FileExtensionChecker.isVideoExtension(photo.getPhotoUrl()))
			.like(likedPost.contains(post))
			.saved(savedPost.contains(post))
			.createdDate(post.getCreatedDate())
			.boundary(post.getBoundary())
			.archiveName(name)
			.build();
	}

}
