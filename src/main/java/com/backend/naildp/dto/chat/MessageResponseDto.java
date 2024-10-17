package com.backend.naildp.dto.chat;

import java.time.LocalDateTime;
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
	private String content;
	private String sender;
	private List<String> mention;
	private String messageType;
	private Long unreadUserCount;
	private List<String> media;
	private LocalDateTime modifiedAt;
	private String profileUrl;

	public static MessageResponseDto of(ChatMessage chatMessage, Long unreadUserCount) {
		return MessageResponseDto.builder()
			.content(chatMessage.getContent())
			.sender(chatMessage.getSender())
			.mention(chatMessage.getMention().stream().toList())
			.messageType(chatMessage.getMessageType())
			.unreadUserCount(unreadUserCount)
			.media(chatMessage.getMedia())
			.modifiedAt(chatMessage.getCreatedAt())
			.profileUrl(chatMessage.getProfileUrl())
			.build();
	}
}