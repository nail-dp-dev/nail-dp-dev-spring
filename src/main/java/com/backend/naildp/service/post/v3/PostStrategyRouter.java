package com.backend.naildp.service.post.v3;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.backend.naildp.service.post.PostStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostStrategyRouter {

	private final Map<String, PostStrategy> postStrategyMap;

	public PostStrategy route(String key) {
		PostStrategy postStrategy = postStrategyMap.get(key);

		if (postStrategy == null) {
			throw new StrategyNotFoundException("No strategy found for key: " + key);
		}

		return postStrategy;
	}
}
