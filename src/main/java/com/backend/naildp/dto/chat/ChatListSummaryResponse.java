package com.backend.naildp.dto.chat;

import java.util.List;

import com.backend.naildp.repository.ChatRoomMapping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatListSummaryResponse {
	List<?> contents;

	public static ChatListSummaryResponse of(List<ChatRoomMapping> contents) {
		return new ChatListSummaryResponse(contents.stream().map(ChatListResponse::of).toList());

	}
}
