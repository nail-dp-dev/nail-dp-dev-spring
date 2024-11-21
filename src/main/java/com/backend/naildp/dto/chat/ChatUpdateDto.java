package com.backend.naildp.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatUpdateDto {
	private UUID chatRoomId;
	private int unreadMessageCount;
	private String lastMessage;
	private LocalDateTime modifiedAt;
	private String sender;
	private String receiver;
}
