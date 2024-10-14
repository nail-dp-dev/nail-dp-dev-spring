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

	// public static MessageSummaryResponse of(List<MessageResponseDto> messageDto, String firstUnreadMessageId,
	// 	List<User> user) {
	//
	// 	return builder()
	// 		.contents(messageDto)
	// 		.firstUnreadMessageId(firstUnreadMessageId)
	// 		.chatUserInfo(chatUserInfoResponses)
	// 		.build();
	// }

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ChatUserInfoResponse {
		private String participant;
		private String profileUrl;
		private Boolean isActive;

		// public ChatUserInfoResponse(String nickname, String thumbnail, Boolean isActive) {
		// 	this.participant = nickname;
		// 	this.profileUrl = thumbnail;
		// 	this.isActive = isActive;
		//
		// }
	}
}
