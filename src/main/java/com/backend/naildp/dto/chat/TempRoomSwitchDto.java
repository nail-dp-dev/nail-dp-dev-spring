package com.backend.naildp.dto.chat;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TempRoomSwitchDto {
	private String sender;
	private UUID newChatRoomId;
}
