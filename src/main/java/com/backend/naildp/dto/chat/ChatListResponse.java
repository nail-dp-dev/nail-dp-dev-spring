package com.backend.naildp.dto.chat;

import java.util.UUID;

import com.backend.naildp.repository.ChatRoomMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatListResponse {
	private String roomName;
	private UUID roomId;
	// private String profileUrl;
	// private String lastMessage;
	// private int unReadMessageCnt;
	// private LocalDateTime modifyingDate;
	// private Boolean isBusiness;

	public static ChatListResponse of(ChatRoomMapping chatRoomInfo) {
		return ChatListResponse.builder()
			.roomName(chatRoomInfo.getName())
			.roomId(chatRoomInfo.getId())
			.build();
	}
}
