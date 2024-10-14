package com.backend.naildp.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatListSummaryResponse {
	List<ChatListResponse> contents;

	public static ChatListSummaryResponse of(List<ChatListResponse> contents) {
		return new ChatListSummaryResponse(contents);

	}
}
