package com.backend.naildp.service.post;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.service.post.dto.UserContext;

public interface PostStrategy {

	boolean isExecutable(UserContext userContext);
	boolean requiresAuthentication();

	PostSummaryResponse homePosts(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsFilteredByTagsAndFollowees(int size, Long cursorPostId, String username);
	PostSummaryResponse homePostsV3(int size, Long cursorPostId, UserContext userContext);
}
