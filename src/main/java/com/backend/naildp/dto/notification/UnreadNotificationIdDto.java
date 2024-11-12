package com.backend.naildp.dto.notification;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnreadNotificationIdDto {
	private List<Long> notificationIds;
}
