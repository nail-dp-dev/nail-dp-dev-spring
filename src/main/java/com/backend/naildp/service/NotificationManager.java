package com.backend.naildp.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.notification.NotificationEventDto;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationManager {

	private final NotificationService notificationService;
	private final NotifyEventHandler notifyEventHandler;

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

	private void sendNotificationEvent(Notification notification) {
		User receiver = notification.getReceiver();
		if (receiver.allowsNotificationType(notification.getNotificationType())) {
			notifyEventHandler.sendWebPushNotification(new NotificationEventDto(notification));
		}
	}


}
