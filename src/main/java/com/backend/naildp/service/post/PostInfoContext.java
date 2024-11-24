package com.backend.naildp.service.post;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.home.PostSummaryResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostInfoContext {

	private final Map<String, PostStrategy> postStrategyMap;

	public PostSummaryResponse posts(String choice, int size, Long cursorPostId) {
		PostStrategy postStrategy = postStrategyMap.get(choice);
		return postStrategy.homePosts(size, cursorPostId, getUsernameFromAuthentication());
	}

	private String getUsernameFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return Objects.isNull(authentication) ? null : authentication.getName();
	}
}
