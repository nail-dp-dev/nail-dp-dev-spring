package com.backend.naildp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageStatusService {
	private final RedisTemplate<String, Object> chatRedisTemplate;
	private static final String UNREAD_USER_KEY = "unread_user";
	private static final String FIRST_UNREAD_MESSAGE_KEY = "first_unread_message";

	// 메시지를 읽지 않은 사용자 추가
	public void addUnreadUser(String chatRoomId, String messageId, String userId) {
		String key = UNREAD_USER_KEY + ":" + chatRoomId + ":" + messageId;
		chatRedisTemplate.opsForSet().add(key, userId);
	}

	// 메시지를 읽은 사용자 제거
	public void removeUnreadUser(String chatRoomId, String messageId, String userId) {
		String key = UNREAD_USER_KEY + ":" + chatRoomId + ":" + messageId;
		chatRedisTemplate.opsForSet().remove(key, userId);
	}

	// 메시지별 읽지 않은 사용자 수 조회
	public Long getUnreadUserCount(String chatRoomId, String messageId) {
		String key = UNREAD_USER_KEY + ":" + chatRoomId + ":" + messageId;
		return chatRedisTemplate.opsForSet().size(key);
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
