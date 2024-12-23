package com.backend.naildp.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.asynchttpclient.Response;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.entity.UserSubscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushAsyncService;
import nl.martijndwars.webpush.Subscription;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

	private final PushAsyncService pushAsyncService;

	public CompletableFuture<Response> sendPush(
		NotificationEventDto notificationEventDto, UserSubscription userSubscription) {
		Subscription subscription = createSubscription(userSubscription);
		try {
			Notification notification = new Notification(subscription, notificationEventDto.getContent());
			return pushAsyncService.send(notification);
		} catch (GeneralSecurityException e) {
			log.error("푸시알림 보안 예외발생: {}", e.getMessage());
			throw new CompletionException(e);
		} catch (IOException | JoseException e) {
			log.error("푸시알림 전송 중 IOException/JoseException 예외발생: {}", e.getMessage());
			throw new CompletionException(e);
		}
	}

	private Subscription createSubscription(UserSubscription userSubscription) {
		Subscription.Keys keys = new Subscription.Keys(userSubscription.getP256dh(), userSubscription.getAuth());
		return new Subscription(userSubscription.getEndpoint(), keys);
	}
}
