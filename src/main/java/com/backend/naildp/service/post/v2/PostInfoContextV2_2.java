package com.backend.naildp.service.post.v2;

import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.service.post.PostStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostInfoContextV2_2 {

	private final Map<String, PostStrategy> postStrategyMap;

	public PostSummaryResponse posts(String choice, int size, Long cursorPostId) {
		PostStrategy postStrategy = postStrategyMap.get(choice);
		return postStrategy.homePostsFilteredByTagsAndFollowees(size, cursorPostId, getUsernameFromAuthentication());
	}

	private String getUsernameFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return Objects.isNull(authentication) ? null : authentication.getName();
	}
}
