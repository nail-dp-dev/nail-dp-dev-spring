package com.backend.naildp.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.dto.notification.PushNotificationDto;
import com.backend.naildp.entity.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyEventHandler {

	private final RedisMessageService redisMessageService;

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
}
