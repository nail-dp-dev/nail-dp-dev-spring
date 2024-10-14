package com.backend.naildp.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSummaryResponse {
	private List<MessageResponseDto> contents;
	private String firstUnreadMessageId;
	private List<ChatUserInfoResponse> chatUserInfo;

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ChatUserInfoResponse {
		private String participant;
		private String profileUrl;
		private Boolean isActive;
		private Boolean isBusiness;

	}
}
