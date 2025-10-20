package com.backend.naildp.service.notification;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.entity.commentEntity.Comment;
import com.backend.naildp.entity.commentEntity.CommentLike;
import com.backend.naildp.entity.userEntity.Follow;
import com.backend.naildp.entity.notificationEntity.Notification;
import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.postEntity.PostLike;
import com.backend.naildp.entity.userEntity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationManager {

	private final NotificationService notificationService;
	private final ApplicationEventPublisher applicationEventPublisher;

	public void handleNotificationFromCommentLike(Comment comment, User user, CommentLike commentLike) {
		if (comment.notRegisteredBy(user)) {
			Notification notificationByCommentLike = Notification.fromCommentLike(commentLike);
			Notification savedNotification = notificationService.save(notificationByCommentLike);

			sendNotificationEvent(savedNotification);
		}
	}

	public void handleCommentNotification(Comment comment, User postWriter) {
		if (comment.notRegisteredBy(postWriter)) {
			Notification notificationByRegisteredComment = Notification.fromComment(comment);
			Notification savedNotification = notificationService.save(notificationByRegisteredComment);

			sendNotificationEvent(savedNotification);
		}
	}

	public void handlePostLikeNotification(User user, Post post, PostLike postLike) {
		if (post.notWrittenBy(user)) {
			Notification notificationByPostLike = Notification.fromPostLike(postLike);
			Notification savedNotification = notificationService.save(notificationByPostLike);

			sendNotificationEvent(savedNotification);
		}
	}

	public void handleFollowNotification(Follow savedFollow) {
		Notification savedNotification = notificationService.save(Notification.followOf(savedFollow));

		sendNotificationEvent(savedNotification);
	}

	private void sendNotificationEvent(Notification notification) {
		User receiver = notification.getReceiver();
		if (receiver.allowsNotificationType(notification.getNotificationType())) {
			applicationEventPublisher.publishEvent(new NotificationEventDto(notification));
		}
	}


}
