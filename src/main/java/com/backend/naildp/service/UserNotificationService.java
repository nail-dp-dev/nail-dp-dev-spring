package com.backend.naildp.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.dto.setting.NotificationSettingResponseDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UserNotification;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.UserNotificationRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

	private final UserNotificationRepository userNotificationRepository;
	private final UserRepository userRepository;

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

	@Transactional
	public void addAllNotificationType(String nickname) {
		User user = userRepository.findUserByNickname(nickname)
			.orElseThrow(() -> new CustomException("회원을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		List<UserNotification> userNotifications = Arrays.stream(NotificationType.values())
			.map(notificationType -> new UserNotification(notificationType, user))
			.collect(Collectors.toList());
		userNotificationRepository.saveAllAndFlush(userNotifications);
	}
}
