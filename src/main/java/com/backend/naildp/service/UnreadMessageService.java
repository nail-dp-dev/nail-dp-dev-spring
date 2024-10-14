package com.backend.naildp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnreadMessageService {
	private final RedisTemplate<String, Object> chatRedisTemplate;
	private static final String UNREAD_COUNT_KEY = "unread_count";
	private static final String FIRST_UNREAD_MESSAGE_KEY = "first_unread_message";

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

	// 특정 채팅방의 사용자별 최초로 읽지 않은 메시지
	public void setFirstUnreadMessageId(String chatRoomId, String userId, String messageId) {
		String key = FIRST_UNREAD_MESSAGE_KEY + ":" + chatRoomId + ":" + userId;
		// 첫 번째 읽지 않은 메시지가 설정되지 않은 경우에만 설정
		chatRedisTemplate.opsForValue().setIfAbsent(key, messageId);
	}

	public String getFirstUnreadMessageId(String chatRoomId, String userId) {
		String key = FIRST_UNREAD_MESSAGE_KEY + ":" + chatRoomId + ":" + userId;
		return (String)chatRedisTemplate.opsForValue().get(key);
	}

	public void resetFirstUnreadMessageId(String chatRoomId, String userId) {
		String key = FIRST_UNREAD_MESSAGE_KEY + ":" + chatRoomId + ":" + userId;
		chatRedisTemplate.delete(key);
	}
}
