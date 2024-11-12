package com.backend.naildp.dto;

import java.time.LocalDateTime;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationResponseDto {

	private String subscriberNickname;
	private String content;
	private NotificationType notificationType;
	private LocalDateTime createdAt;

	public static PushNotificationResponseDto from(Notification notification) {
		return PushNotificationResponseDto.builder()
			.subscriberNickname(notification.getReceiver().getNickname())
			.content(notification.getContent())
			.notificationType(notification.getNotificationType())
			.createdAt(notification.getCreatedDate())
			.build();
	}
}


