package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.notification.SubscriptionUpdateDto;
import com.backend.naildp.entity.User;
import com.backend.naildp.entity.UserSubscription;
import com.backend.naildp.repository.UserRepository;
import com.backend.naildp.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

	private final UserSubscriptionRepository userSubscriptionRepository;
	private final UserRepository userRepository;

	@Transactional
	public void updateSubscription(SubscriptionUpdateDto subscriptionUpdateDto, String nickname) {
		userSubscriptionRepository.findSubscriptionAndUserByNickname(nickname)
			.ifPresentOrElse(findMemberSubscription -> updateSubscriptionInfo(findMemberSubscription, subscriptionUpdateDto),
				() -> registerSubscriptionInfo(nickname, subscriptionUpdateDto));
	}

	private void updateSubscriptionInfo(UserSubscription userSubscription, SubscriptionUpdateDto subscriptionUpdateDto) {
		if (userSubscription.hasDifferentSubscriptionInfo(subscriptionUpdateDto.getEndpoint())) {
			userSubscription.updateInfo(subscriptionUpdateDto.getEndpoint(), subscriptionUpdateDto.getP256dh(),
				subscriptionUpdateDto.getAuth());
		}
	}

	private void registerSubscriptionInfo(String username, SubscriptionUpdateDto subscriptionUpdateDto) {
		User user = userRepository.findByNickname(username)
			.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		UserSubscription userSubscription = subscriptionUpdateDto.toEntity(user);
		userSubscriptionRepository.save(userSubscription);
	}
}
