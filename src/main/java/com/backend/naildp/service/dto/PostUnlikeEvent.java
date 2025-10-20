package com.backend.naildp.service.dto;

import java.util.UUID;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUnlikeEvent {

	private Long likedPostId;
	private UUID memberId;

	public PostUnlikeEvent(Post post, User user) {
		this.likedPostId = post.getId();
		this.memberId = user.getId();
	}
}
