package com.backend.naildp.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.NotifyService;
import com.backend.naildp.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Subscription;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SubscriptionController {

	private final NotifyService notifyService;
	private final SubscriptionService subscriptionService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return ResponseEntity.ok(notifyService.subscribe(userDetails.getUser().getNickname()));
	}

	@PostMapping("/subscribe")
	public ResponseEntity<?> subscribeWeb(@RequestBody Subscription subscription,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		subscriptionService.updateSubscription(subscription, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "구독 정보 저장", 2001));
	}
}
