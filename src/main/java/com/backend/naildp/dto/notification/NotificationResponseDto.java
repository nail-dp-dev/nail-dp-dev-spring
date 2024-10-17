package com.backend.naildp.dto.notification;

import java.time.LocalDateTime;

import com.backend.naildp.common.NotificationType;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationResponseDto {

	private Long notificationId;
	private String senderNickname;
	private String senderProfileUrl;
	private String notificationContent;
	private NotificationType notificationType;
	private Boolean isRead;
	private LocalDateTime createdDate;
	private String link;

	@QueryProjection
	public NotificationResponseDto(Long notificationId, String senderNickname, String senderProfileUrl,
		String notificationContent, NotificationType notificationType, Boolean isRead, LocalDateTime createdDate,
		String link) {
		this.notificationId = notificationId;
		this.senderNickname = senderNickname;
		this.senderProfileUrl = senderProfileUrl;
		this.notificationContent = notificationContent;
		this.notificationType = notificationType;
		this.isRead = isRead;
		this.createdDate = createdDate;
		this.link = link;
	}
}
