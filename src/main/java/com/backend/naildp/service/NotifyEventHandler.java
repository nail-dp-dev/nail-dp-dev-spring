package com.backend.naildp.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.dto.notification.PushNotificationDto;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyEventHandler {

	private final RedisMessageService redisMessageService;
	private final WebPushService webPushService;
	private final UserSubscriptionRepository userSubscriptionRepository;

	/**
	 * 알림 이벤트 메시지 밣행 -> redisMessageService
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishNotificationEvent(Notification notification) {
		log.info("NotifyEventHandler 푸시알림 : {}", notification.getNotificationType());
		log.info("receiver nickname : {}", notification.getReceiver().getNickname());
		redisMessageService.publish(notification.getReceiver().getNickname(),
			PushNotificationDto.from(notification));
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendWebPushNotification(NotificationEventDto notificationEventDto) {
		userSubscriptionRepository.findByUserNickname(notificationEventDto.getReceiverNickname())
			.ifPresent(userSubscription -> webPushService.sendPush(notificationEventDto, userSubscription)
				.handle((response, ex) -> {
					if (ex != null) {
						log.error("푸시 알림 전송 실패: {}", ex.getMessage());
						return null;
					}
					log.info("푸시 알림 전송 성공");
					return response;
				}));

	}
}
