package com.backend.naildp.entity.mongo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.backend.naildp.dto.chat.ChatMessageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "chat_message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
	@Id
	private String id;

	private List<String> content;

	private Boolean status = false;

	@CreatedDate
	private LocalDateTime createdAt;

	private String sender;

	private UUID chatRoomId;

	private List<String> mention;

	private String messageType;

	public ChatMessage(ChatMessageDto chatMessageDto, UUID chatRoomId) {
		this.chatRoomId = chatRoomId;
		this.content = chatMessageDto.getContent();
		this.sender = chatMessageDto.getSender();
		this.mention = chatMessageDto.getMention();
	}
}
