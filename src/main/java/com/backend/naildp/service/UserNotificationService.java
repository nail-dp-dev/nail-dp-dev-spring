package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.dto.setting.NotificationSettingResponseDto;
import com.backend.naildp.entity.UserNotification;
import com.backend.naildp.repository.UserNotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

	private final UserNotificationRepository userNotificationRepository;

	@Transactional(readOnly = true)
	public List<NotificationSettingResponseDto> getNotificationSetting(String nickname) {
		List<UserNotification> userNotifications = userNotificationRepository.findNotificationTypeByUserNickname(nickname);
		return userNotifications.stream().map(NotificationSettingResponseDto::new).toList();
	}

	@Transactional
	public void changeNotificationSetting(NotificationType notificationType, Boolean status, String nickname) {
		int count = userNotificationRepository.updateUserNotificationByNotificationType(status, nickname, notificationType);
	}

	@Transactional
	public void changeAllNotificationType(Boolean status, String nickname) {
		int count = userNotificationRepository.updateAllNotificationType(status, nickname);
	}
}
