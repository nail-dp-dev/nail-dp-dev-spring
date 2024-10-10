package com.backend.naildp.dto.chat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.backend.naildp.repository.ChatRoomMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponseDto {
	private List<String> roomName;
	private List<UUID> roomId;

	public static MessageListResponseDto of(List<ChatRoomMapping> chatRoomList) {
		return MessageListResponseDto.builder()
			.roomName(chatRoomList.stream().map(ChatRoomMapping::getName).collect(Collectors.toList()))
			.roomId(chatRoomList.stream().map(ChatRoomMapping::getId).collect(Collectors.toList()))
			.build();
	}
}
