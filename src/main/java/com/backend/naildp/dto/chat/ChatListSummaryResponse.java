package com.backend.naildp.dto.chat;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatListSummaryResponse {
	private UUID cursorId;
	private Slice<ChatListResponse> contents;

	public static ChatListSummaryResponse of(Slice<ChatListResponse> contents) {
		UUID cursorId = contents.getContent().get(contents.getNumberOfElements() - 1).getRoomId();
		return new ChatListSummaryResponse(cursorId, contents);

	}

	public static ChatListSummaryResponse createEmptyResponse() {
		return new ChatListSummaryResponse(null, new SliceImpl<>(new ArrayList<>()));
	}
}
