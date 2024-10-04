package com.backend.naildp.service.post;

import com.backend.naildp.dto.home.PostSummaryResponse;

public interface PostStrategy {

	PostSummaryResponse homePosts(int size, Long cursorPostId, String username);
}
