package com.backend.naildp.service.chat;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomStatusService {
	private final RedisTemplate<String, Object> chatRedisTemplate;
	private static final String UNREAD_COUNT_KEY = "unread_count";

	// 안읽은 메시지 카운트
	public void incrementUnreadCount(String chatRoomId, String userId) {
		String key = UNREAD_COUNT_KEY + ":" + chatRoomId + ":" + userId;
		chatRedisTemplate.opsForValue().increment(key);
	}

	public void resetUnreadCount(String chatRoomId, String userId) {
		String key = UNREAD_COUNT_KEY + ":" + chatRoomId + ":" + userId;
		chatRedisTemplate.delete(key);
	}

	public Integer getUnreadCount(String chatRoomId, String userId) {
		String key = UNREAD_COUNT_KEY + ":" + chatRoomId + ":" + userId;
		Integer count = (Integer)chatRedisTemplate.opsForValue().get(key);
		return count != null ? count : 0;  // null이면 0 반환
	}

}