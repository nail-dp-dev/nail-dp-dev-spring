package com.backend.naildp.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
	private List<ContentDto> content;
	private Boolean status;
	private Long userId;
	private Long chatRoomId;
	private Boolean mention;

	@Getter
	public static class ContentDto {
		private String type;
		private String value;
	}
}