package com.backend.naildp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.NotificationRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final SseService sseService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public long generateFollowNotification(Follow follow) {
		log.info("follow transactionalEventListener -> 새로운 transaction 시작");
		User followerUser = follow.getFollower();
		User followingUser = follow.getFollowing();

		Notification followNotification = Notification.builder()
			.receiver(followingUser)
			.content(followerUser.getNickname() + "님이 회원님을 팔로우했습니다.")
			.notificationType(NotificationType.FOLLOW)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(followNotification);

		//팔로우 푸시 알림 전송
		sseService.sendFollowPush(followerUser.getNickname(), savedNotification);

		return savedNotification.getId();
	}

	@Transactional
	public long generatePostLikeNotification(User sender, Post likedPost) {
		Notification postLikeNotification = Notification.builder()
			.receiver(likedPost.getUser())
			.content(sender.getNickname() + "가 회원님의 게시물을 좋아합니다.")
			.notificationType(NotificationType.POST_LIKE)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(postLikeNotification);

		sseService.sendPushNotification(sender.getNickname(), savedNotification);

		return savedNotification.getId();
	}
}
