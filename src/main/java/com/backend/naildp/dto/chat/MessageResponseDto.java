package com.backend.naildp.dto.chat;

import java.util.List;

import com.backend.naildp.entity.mongo.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
	private List<String> content;
	private String sender;
	private List<String> mention;
	private String messageType;
	// private Long unReadMessageCnt;

	public static MessageResponseDto of(ChatMessage chatMessage) {
		return MessageResponseDto.builder()
			.content(chatMessage.getContent().stream().toList())
			.sender(chatMessage.getSender())
			.mention(chatMessage.getMention().stream().toList())
			.messageType(chatMessage.getMessageType())
			.build();
	}
}