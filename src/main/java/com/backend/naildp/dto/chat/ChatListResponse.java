package com.backend.naildp.dto.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.backend.naildp.repository.ChatRoomMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatListResponse {
	private String roomName;
	private UUID roomId;
	private int unreadCount;
	private List<String> profileUrls;
	private String lastMessage;
	private int participantCnt;
	private LocalDateTime modifiedAt;
	private Boolean isBusiness;
	private Boolean isPinning;

	public static ChatListResponse of(ChatRoomMapping chatRoomInfo, int unreadCount, List<String> profileUrls) {
		return ChatListResponse.builder()
			.roomName(chatRoomInfo.getName())
			.roomId(chatRoomInfo.getId())
			.unreadCount(unreadCount)
			.lastMessage(chatRoomInfo.getLastMessage())
			.profileUrls(profileUrls)
			.participantCnt(Optional.ofNullable(chatRoomInfo.getParticipantCnt()).orElse(0))
			.modifiedAt(chatRoomInfo.getModifiedAt())
			.isBusiness(false)
			.isPinning(chatRoomInfo.getIsPinning())
			.build();
	}
}
