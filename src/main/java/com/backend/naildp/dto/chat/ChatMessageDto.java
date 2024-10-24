package com.backend.naildp.dto.chat;

import java.util.List;

import com.backend.naildp.entity.mongo.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ChatMessageDto {
	private String content;
	private String sender;
	private List<String> mention;
	private String messageType;
	private String chatRoomId;
	private List<String> media;

	public static ChatMessageDto of(ChatMessage chatMessage) {
		return ChatMessageDto.builder()
			.chatRoomId(chatMessage.getChatRoomId())
			.sender(chatMessage.getSender())
			.messageType(chatMessage.getMessageType())
			.content(chatMessage.getContent())
			.media(chatMessage.getMedia())
			.mention(chatMessage.getMention())
			.build();
	}
}