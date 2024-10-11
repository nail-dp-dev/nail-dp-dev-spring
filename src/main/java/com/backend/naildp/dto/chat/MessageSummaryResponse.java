package com.backend.naildp.dto.chat;

import java.util.List;

import com.backend.naildp.entity.mongo.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSummaryResponse {
	List<MessageResponseDto> contents;

	public static MessageSummaryResponse of(List<ChatMessage> chatMessageList) {
		return new MessageSummaryResponse(
			chatMessageList.stream().map(MessageResponseDto::of).toList());
	}
}
