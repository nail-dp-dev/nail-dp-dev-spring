package com.backend.naildp.service.post.v2;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.service.post.PostStrategy;
import com.backend.naildp.service.post.dto.UserContext;
import com.backend.naildp.service.post.v3.PostStrategyRouter;
import com.backend.naildp.service.post.v3.UserContextProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostInfoContextV3 {

	private final PostStrategyRouter postStrategyRouter;
	private final UserContextProvider userContextProvider;

	public PostSummaryResponse posts(String choice, int size, Long cursorPostId) {
		PostStrategy postStrategy = postStrategyRouter.route(choice);
		UserContext userContext = userContextProvider.getUsernameFromAuthentication();

		if (!postStrategy.isExecutable(userContext)) {
			throw new AccessDeniedException("You are not authenticated");
		}

		return postStrategy.homePostsV3(size, cursorPostId, userContext);
	}

}
