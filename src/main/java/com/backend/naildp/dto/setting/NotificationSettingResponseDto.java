package com.backend.naildp.dto.setting;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.UserNotification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingResponseDto {

	private NotificationType notificationType;
	private boolean isEnabled;

	public NotificationSettingResponseDto(UserNotification userNotification) {
		this.notificationType = userNotification.getNotificationType();
		this.isEnabled = userNotification.isEnabled();
	}
}
