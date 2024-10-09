package com.backend.naildp.dto.chat;

import java.util.List;
import java.util.stream.Collectors;

import com.backend.naildp.entity.ChatRoomUser;

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

	public static MessageListResponseDto of(List<ChatRoomUser> chatRoomUsers) {
		return MessageListResponseDto.builder()
			.roomName(chatRoomUsers.stream().map(ChatRoomUser::getName).collect(Collectors.toList()))
			.build();
	}
}
