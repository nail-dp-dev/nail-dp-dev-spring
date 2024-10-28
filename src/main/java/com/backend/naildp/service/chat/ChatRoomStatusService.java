package com.backend.naildp.service.chat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomStatusService {
	private final RedisTemplate<String, Object> chatRedisTemplate;
	private static final String UNREAD_COUNT_KEY = "unread_count";
	private static final String RECENT_USERS_KEY = "recent_users";

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

	// 특정 사용자의 최근 대화 상대를 추가하는 메서드
	public void addRecentUsers(String username, String chatPartner, long timestamp) {
		chatRedisTemplate.opsForZSet().add(RECENT_USERS_KEY + ":" + username, chatPartner, timestamp);
		limitRecentUsers(username); // 추가 후 제한 적용
	}

	// 특정 사용자의 최근 대화 목록을 10개로 제한하는 메서드
	public void limitRecentUsers(String username) {
		chatRedisTemplate.opsForZSet().removeRange(RECENT_USERS_KEY + ":" + username, 0, -11);
	}

	// 특정 사용자의 최근 대화 상대 목록을 가져오는 메서드
	public List<String> getRecentUsers(String username) {
		Set<Object> recentUsers = chatRedisTemplate.opsForZSet()
			.reverseRange(RECENT_USERS_KEY + ":" + username, 0, 9);

		if (recentUsers == null) {
			return Collections.emptyList();
		}
		return recentUsers.stream()
			.map(Object::toString)
			.collect(Collectors.toList());
	}
}
