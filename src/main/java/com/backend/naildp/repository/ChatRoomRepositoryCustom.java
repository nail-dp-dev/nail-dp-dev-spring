package com.backend.naildp.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.backend.naildp.dto.chat.ChatListResponse;

public interface ChatRoomRepositoryCustom {

	Slice<ChatListResponse> searchChatRoomsByName(String roomNameKeyword, Pageable pageable, UUID cursorId,
		String nickname);

}
