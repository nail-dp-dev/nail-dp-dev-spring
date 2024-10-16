package com.backend.naildp.dto;

import java.time.LocalDateTime;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationResponseDto {

	private String publisherNickname;
	private String subscriberNickname;
	private String content;
	private NotificationType notificationType;
	private LocalDateTime createdAt;

	public static PushNotificationResponseDto create(String nickname, Notification notification) {
		return PushNotificationResponseDto.builder()
			.publisherNickname(nickname)
			.subscriberNickname(notification.getReceiver().getNickname())
			.content(notification.getContent())
			.notificationType(notification.getNotificationType())
			.createdAt(notification.getCreatedDate())
			.build();
	}
}


