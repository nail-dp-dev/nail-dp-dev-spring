package com.backend.naildp.service.post;

import com.backend.naildp.dto.home.PostSummaryResponse;

public interface PostStrategy {

	PostSummaryResponse homePosts(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsWithoutTagPostJoinAndFollow(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsV3(int size, Long cursorPostId, String username);
}
