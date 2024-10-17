package com.backend.naildp.dto.notification;

import java.time.LocalDateTime;

import com.backend.naildp.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponseDto {

	private String senderNickname;
	private String senderProfileUrl;
	private Boolean isRead;
	private String notificationContent;
	private LocalDateTime createdDate;

	public static NotificationResponseDto from(Notification notification) {
		return null;
	}

}
