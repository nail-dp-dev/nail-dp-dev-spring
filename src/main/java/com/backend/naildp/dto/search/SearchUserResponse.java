package com.backend.naildp.dto.search;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchUserResponse {

	private String nickname;
	private String profileUrl;
	private Long postCount;
	private Long savedPostCount;
	private Long followerCount;
	private Boolean isFollowing;

	@QueryProjection
	public SearchUserResponse(String nickname, String profileUrl, Long postCount, Long savedPostCount,
		Long followerCount, Boolean isFollowing) {
		this.nickname = nickname;
		this.profileUrl = profileUrl;
		this.postCount = postCount;
		this.savedPostCount = savedPostCount;
		this.followerCount = followerCount;
		this.isFollowing = isFollowing;
	}
}
