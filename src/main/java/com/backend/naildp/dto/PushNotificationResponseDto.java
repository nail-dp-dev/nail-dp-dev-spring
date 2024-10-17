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

	private String senderNickname;
	private String receiverNickname;
	private String content;
	private NotificationType notificationType;
	private LocalDateTime createdAt;

	public static PushNotificationResponseDto from(String senderName, Notification notification) {
		return PushNotificationResponseDto.builder()
			.senderNickname(senderName)
			.receiverNickname(notification.getReceiver().getNickname())
			.content(notification.getContent())
			.notificationType(notification.getNotificationType())
			.createdAt(notification.getCreatedDate())
			.build();
	}

	public static PushNotificationResponseDto fromV2(Notification notification) {
		return PushNotificationResponseDto.builder()
			.senderNickname(notification.getSender().getNickname())
			.receiverNickname(notification.getReceiver().getNickname())
			.content(notification.getContent())
			.notificationType(notification.getNotificationType())
			.createdAt(notification.getCreatedDate())
			.build();
	}
}


