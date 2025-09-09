package com.backend.naildp.service.post;

public class CacheKeyGenerator {

	private static final String LIKED_POSTS_KEY_PREFIX = "likedPosts:";
	private static final String SAVED_POSTS_KEY_PREFIX = "savedPosts:";

	public static String getSavedPostsKey(String username) {
		return SAVED_POSTS_KEY_PREFIX + username;
	}

	public static String getLikedPostsKey(String username) {
		return LIKED_POSTS_KEY_PREFIX + username;
	}
}
