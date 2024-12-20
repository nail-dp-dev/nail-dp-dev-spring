package com.backend.naildp.dto.notification;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDto {

	private String receiverNickname;
	private String content;
	private NotificationType notificationType;

	public NotificationEventDto(Notification notification) {
		receiverNickname = notification.getReceiver().getNickname();
		content = notification.getContent();
		notificationType = notification.getNotificationType();
	}
}
