package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UserSubscription;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Subscription;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

	private final UserSubscriptionRepository userSubscriptionRepository;
	private final UserRepository userRepository;

	@Transactional
	public void updateSubscription(Subscription subscription, String nickname) {
		userSubscriptionRepository.findSubscriptionAndUserByNickname(nickname)
			.ifPresentOrElse(findMemberSubscription -> updateSubscriptionInfo(findMemberSubscription, subscription),
				() -> registerSubscriptionInfo(nickname, subscription));
	}

	private void updateSubscriptionInfo(UserSubscription userSubscription, Subscription subscription) {
		if (userSubscription.hasDifferentSubscriptionInfo(subscription)) {
			userSubscription.updateInfo(subscription);
		}
	}

	private void registerSubscriptionInfo(String username, Subscription subscription) {
		User user = userRepository.findByNickname(username)
			.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		UserSubscription userSubscription = UserSubscription.of(user, subscription);
		userSubscriptionRepository.save(userSubscription);
	}
}
