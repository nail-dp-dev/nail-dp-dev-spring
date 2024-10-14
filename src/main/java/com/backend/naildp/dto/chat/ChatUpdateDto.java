package com.backend.naildp.dto.chat;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatUpdateDto {
	private String chatRoomId;
	private int unreadMessageCount;
	private String lastMessage;
	private LocalDateTime modifiedAt;
}
