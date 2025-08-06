package com.backend.naildp.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyEventHandler {

	private final WebPushService webPushService;
	private final UserSubscriptionRepository userSubscriptionRepository;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
