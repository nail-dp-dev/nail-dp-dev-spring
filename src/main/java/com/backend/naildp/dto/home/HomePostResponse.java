package com.backend.naildp.dto.home;

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

}
