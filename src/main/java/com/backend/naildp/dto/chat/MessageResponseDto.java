package com.backend.naildp.dto.chat;

import java.util.List;
import java.util.stream.Collectors;

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

	public static MessageResponseDto of(List<ChatMessage> chatMessageList) {
		return MessageResponseDto.builder()
			.content(chatMessageList.stream()
				.flatMap(chatMessage -> chatMessage.getContent().stream())
				.collect(Collectors.toList()))
			.sender(chatMessageList.isEmpty() ? null : chatMessageList.get(0).getSender())
			.mention(chatMessageList.stream()
				.flatMap(msg -> msg.getMention().stream())
				.collect(Collectors.toList()))
			.messageType(chatMessageList.isEmpty() ? null : chatMessageList.get(0).getMessageType())
			.build();
	}
}