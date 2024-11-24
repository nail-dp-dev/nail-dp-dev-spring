package com.backend.naildp.dto.notification;

import java.time.LocalDateTime;

import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationDto {

	private String receiverNickname;
	private String senderNickname;
	private String senderProfileUrl;
	private String content;
	private LocalDateTime createdAt;

	public static PushNotificationDto from(Notification notification) {
		User sender = notification.getSender();
		User receiver = notification.getReceiver();
		return PushNotificationDto.builder()
			.receiverNickname(receiver.getNickname())
			.senderNickname(sender.getNickname())
			.senderProfileUrl(sender.getThumbnailUrl())
			.content(notification.getContent())
			.createdAt(notification.getCreatedDate())
			.build();
	}
}
