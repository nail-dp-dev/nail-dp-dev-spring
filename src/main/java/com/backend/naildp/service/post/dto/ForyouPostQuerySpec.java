package com.backend.naildp.service.post.dto;

import java.util.List;
import java.util.UUID;

import com.backend.naildp.entity.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ForyouPostQuerySpec {

	private Post cursorPost;
	private List<Long> tagIdsInPosts;
	private List<UUID> readableUserIds;

	private ForyouPostQuerySpec(Post cursorPost, List<Long> tagIdsInPosts, List<UUID> readableUserIds) {
		this.cursorPost = cursorPost;
		this.tagIdsInPosts = tagIdsInPosts;
		this.readableUserIds = readableUserIds;
	}

	public static ForyouPostQuerySpec of(Post cursorPost, List<Long> tagIdsInPosts, List<UUID> readableUserIds) {
		return new ForyouPostQuerySpec(cursorPost, tagIdsInPosts, readableUserIds);
	}
}
