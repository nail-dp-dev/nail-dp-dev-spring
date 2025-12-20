package com.backend.naildp.service.handler;

import com.backend.naildp.service.dto.PostLikeEventDto;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.service.dto.PostLikeEvent;
import com.backend.naildp.service.dto.PostUnlikeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeEventHandler {

	private final PostLikeUpdater postLikeUpdater;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void increase(PostLikeEvent postLikeEvent) {
		Long likedPostId = postLikeEvent.getLikedPostId();
		UUID userId = postLikeEvent.getMemberId();

		postLikeUpdater.increaseLikeCount(likedPostId, userId);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void decrease(PostUnlikeEvent postUnlikeEvent) {
		Long likedPostId = postUnlikeEvent.getLikedPostId();
		UUID userId = postUnlikeEvent.getMemberId();

		postLikeUpdater.decreaseLikeCount(likedPostId, userId);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public CompletableFuture<Void> increaseLikeCountAfterSaveLike(PostLikeEventDto postLikeEventDto) {
		return CompletableFuture.runAsync(() -> {
			Long likedPostId = postLikeEventDto.getPostId();
			UUID likeUserId = postLikeEventDto.getWriterId();

			postLikeUpdater.increaseLikeCountConcurrently(likedPostId, likeUserId);
		});
	}
}
