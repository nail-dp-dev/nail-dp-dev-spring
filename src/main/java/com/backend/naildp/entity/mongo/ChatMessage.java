package com.backend.naildp.entity.mongo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

	private List<Content> content;

	private Boolean status;

	@CreatedDate
	private LocalDateTime createdAt;

	private Long userId;

	private Long chatRoomId;

	private Boolean mention;

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Content {
		private String type; // text or media
		private String value;

	}
}
