package com.backend.naildp.dto.chat;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TempRoomSwitchDto {
	private String sender;
	private UUID newChatRoomId;
}
