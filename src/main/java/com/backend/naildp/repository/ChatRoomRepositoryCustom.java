package com.backend.naildp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.dto.chat.ChatListResponse;
import com.backend.naildp.entity.ChatRoom;

public interface ChatRoomRepositoryCustom {

	Slice<ChatListResponse> searchChatRoomsByName(String roomNameKeyword, Pageable pageable, UUID cursorId,
		String nickname);

	Optional<ChatRoom> findMostRecentChatRoomByDuplicatedGroup(List<String> userNames);

}
