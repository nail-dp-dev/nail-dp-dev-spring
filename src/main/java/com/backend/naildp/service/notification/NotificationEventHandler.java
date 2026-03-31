package com.backend.naildp.service.notification;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.entity.notificationEntity.Notification;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;
import com.backend.naildp.repository.user.UserSubscriptionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.entity.commentEntity.Comment;
import com.backend.naildp.entity.commentEntity.CommentLike;
import com.backend.naildp.entity.postEntity.PostLike;
import com.backend.naildp.entity.userEntity.Follow;
import com.backend.naildp.repository.comment.CommentLikeRepository;
import com.backend.naildp.repository.comment.CommentRepository;
import com.backend.naildp.repository.post.PostLikeRepository;
import com.backend.naildp.repository.user.FollowRepository;
import com.backend.naildp.service.comment.dto.event.CommentCreatedEvent;
import com.backend.naildp.service.comment.dto.event.CommentLikedEvent;
import com.backend.naildp.service.post.dto.event.PostLikeNotificationEvent;
import com.backend.naildp.service.user.dto.event.UserFollowedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationManager notificationManager;
	private final PostLikeRepository postLikeRepository;
	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final FollowRepository followRepository;
	private final NotificationService notificationService;
	private final UserSubscriptionRepository userSubscriptionRepository;
	private final WebPushService webPushService;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePostLike(PostLikeNotificationEvent event) {
		PostLike postLike = postLikeRepository.findById(event.postLikeId()).orElseThrow();
		notificationManager.handlePostLikeNotification(postLike.getUser(), postLike.getPost(), postLike);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleCommentCreated(CommentCreatedEvent event) {
		Comment comment = commentRepository.findById(event.commentId()).orElseThrow();
		notificationManager.handleCommentNotification(comment, comment.getPost().getUser());
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleCommentLiked(CommentLikedEvent event) {
		CommentLike commentLike = commentLikeRepository.findById(event.commentLikeId()).orElseThrow();
		notificationManager.handleNotificationFromCommentLike(
			commentLike.getComment(), commentLike.getUser(), commentLike);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserFollowed(UserFollowedEvent event) {
		Follow follow = followRepository.findById(event.followId()).orElseThrow();
		notificationManager.handleFollowNotification(follow);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePostLikeV2(PostLikeNotificationEvent event) {
		PostLike postLike = postLikeRepository.findById(event.postLikeId()).orElseThrow();
		Post post = postLike.getPost();
		User likeUser = postLike.getUser();

		if (post.isWrittenBy(likeUser)) {
			return;
		}

		// 알림 저장
		Notification notification = Notification.fromPostLike(postLike);
		Notification savedNotification = notificationService.save(notification);

		// 알림 발송
		User receiver = savedNotification.getReceiver();
		if (receiver.allowsNotificationType(savedNotification.getNotificationType())) {
			userSubscriptionRepository.findByUserId(receiver.getId())
					.ifPresent(userSubscription -> {
								NotificationEventDto notificationEventDto = new NotificationEventDto(savedNotification);
								webPushService.sendPush(notificationEventDto, userSubscription);
							}
					);
		}
	}
}
