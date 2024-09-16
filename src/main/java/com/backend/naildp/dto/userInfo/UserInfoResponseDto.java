package com.backend.naildp.dto.userInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
	private String nickname;
	private int postsCount;
	private int saveCount;
	private Long point;
	private String profileUrl;
	private int followerCount;
	private int followingCount;
	private Boolean followingStatus;

}
