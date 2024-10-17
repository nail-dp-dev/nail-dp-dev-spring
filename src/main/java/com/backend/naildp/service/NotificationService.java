package com.backend.naildp.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.common.NotificationType;
import com.backend.naildp.dto.notification.NotificationResponseDto;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
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

	/**
	 * 팔로우 알림 생성 및 푸시알림 전송
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public long generateFollowNotification(Follow follow) {
		log.info("follow transactionalEventListener -> 새로운 transaction 시작");
		User followerUser = follow.getFollower();
		User followingUser = follow.getFollowing();

		Notification followNotification = Notification.builder()
			.receiver(followingUser)
			.sender(followerUser)
			.content(followerUser.getNickname() + "님이 회원님을 팔로우했습니다.")
			.notificationType(NotificationType.FOLLOW)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(followNotification);

		//팔로우 푸시 알림 전송
		// sseService.sendFollowPush(followerUser.getNickname(), savedNotification);
		sseService.sendPushNotification(savedNotification);

		return savedNotification.getId();
	}

	/**
	 * 게시물 좋아요 알림 생성 및 푸시알림 전송
	 */
	@Transactional
	public long generatePostLikeNotification(User sender, Post likedPost) {
		Notification postLikeNotification = Notification.builder()
			.receiver(likedPost.getUser())
			.sender(sender)
			.content(sender.getNickname() + "가 회원님의 게시물을 좋아합니다.")
			.notificationType(NotificationType.POST_LIKE)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(postLikeNotification);

		sseService.sendPushNotification(savedNotification);

		return savedNotification.getId();
	}

	/**
	 * 댓글 좋아요 알림 생성 및 푸시알림 전송
	 */
	@Transactional
	public long generateCommentLikeNotification(User sender, Comment comment) {
		Notification commentLikeNotification = Notification.builder()
			.receiver(comment.getUser())
			.sender(sender)
			.content(sender.getNickname() + "님이 회원님의 댓글을 좋아합니다.")
			.notificationType(NotificationType.COMMENT_LIKE)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(commentLikeNotification);

		sseService.sendPushNotification(savedNotification);

		return savedNotification.getId();
	}

	/**
	 * 게시물에 댓글 등록 알림 생성 및 푸시알림 전송
	 */
	@Transactional
	public long generateCommentNotification(User sender, Comment comment) {
		Notification commentNotification = Notification.builder()
			.receiver(comment.getUser())
			.sender(sender)
			.content(sender.getNickname() + "님이 회원님의 게시물에 댓글을 등록했습니다. " + comment.getCommentContent())
			.notificationType(NotificationType.COMMENT)
			.isRead(false)
			.build();

		Notification savedNotification = notificationRepository.save(commentNotification);

		sseService.sendPushNotification(savedNotification);

		return savedNotification.getId();
	}

	public Slice<NotificationResponseDto> allNotifications(String username) {
		return notificationRepository.findNotificationSliceByUsername(username);
	}
}
