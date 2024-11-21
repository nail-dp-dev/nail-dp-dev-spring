package com.backend.naildp.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.dto.notification.NotificationResponseDto;
import com.backend.naildp.dto.notification.PushNotificationDto;
import com.backend.naildp.dto.notification.UnreadNotificationIdDto;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.repository.EmitterRepository;
import com.backend.naildp.repository.NotificationRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final RedisMessageService redisMessageService;

	@Transactional
	public Notification save(Notification notification) {
		return notificationRepository.save(notification);
	}

	@Transactional(readOnly = true)
	public Slice<NotificationResponseDto> allNotifications(Pageable pageable, String username) {
		return notificationRepository.findNotificationSliceByUsername(pageable, username);
	}

	@Transactional
	public void readNotifications(UnreadNotificationIdDto unreadNotificationIdDto, String nickname) {
		List<Notification> unreadNotifications = notificationRepository.findNotificationsByIdInAndReceiverNickname(
			unreadNotificationIdDto.getNotificationIds(), nickname);
		notificationRepository.changeReadStatus(unreadNotifications);
	}

	/**
	 * 알림 이벤트 메시지 밣행 -> redisMessageService
	 */
	public void publishNotificationEvent(Notification notification) {
		log.info("푸시알림 v2");
		log.info("receiver nickname : {}", notification.getReceiver().getNickname());
		redisMessageService.publish(notification.getReceiver().getNickname(),
			PushNotificationDto.from(notification));
	}
}
