package com.backend.naildp.dto.home;

import java.util.List;

import com.backend.naildp.entity.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomePostResponse {

	private Long postId;
	private Long photoId;
	private String photoUrl;
	private Boolean like;
	private Boolean saved;

	public HomePostResponse(Post post, List<Post> savedPosts, List<Post> likedPosts) {
		postId = post.getId();
		photoId = post.getPhotos().get(0).getId();
		photoUrl = post.getPhotos().get(0).getPhotoUrl();
		like = likedPosts.contains(post);
		saved = savedPosts.contains(post);
	}
}
