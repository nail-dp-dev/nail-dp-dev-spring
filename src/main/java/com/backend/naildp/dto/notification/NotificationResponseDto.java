package com.backend.naildp.dto.notification;

import java.time.LocalDateTime;

import com.backend.naildp.entity.Notification;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class NotificationResponseDto {

	private String senderNickname;
	private String senderProfileUrl;
	private Boolean isRead;
	private String notificationContent;
	private LocalDateTime createdDate;

	@QueryProjection
	public NotificationResponseDto(String senderNickname, String senderProfileUrl, Boolean isRead,
		String notificationContent, LocalDateTime createdDate) {
		this.senderNickname = senderNickname;
		this.senderProfileUrl = senderProfileUrl;
		this.isRead = isRead;
		this.notificationContent = notificationContent;
		this.createdDate = createdDate;
	}
}
