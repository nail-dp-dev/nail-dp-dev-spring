package com.backend.naildp.service.handler;

import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.service.dto.PostLikeEvent;
import com.backend.naildp.service.dto.PostUnlikeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeEventHandler {

	private final PostLikeRepository postLikeRepository;
	private final PostRepository postRepository;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void increase(PostLikeEvent postLikeEvent) {
		Long likedPostId = postLikeEvent.getLikedPostId();
		UUID userId = postLikeEvent.getMemberId();

		postLikeRepository.findPostLikeByPostIdAndUserId(likedPostId, userId)
			.ifPresentOrElse(postLike -> {
					Post post = postLike.getPost();
					post.increaseLike();
				},
				() -> log.error("Like Post Event By postId:{}, userId:{}. PostLike Entity가 존재하지 않습니다.", likedPostId, userId)
			);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void decrease(PostUnlikeEvent postUnlikeEvent) {
		Long likedPostId = postUnlikeEvent.getLikedPostId();
		UUID userId = postUnlikeEvent.getMemberId();

		boolean exists = postLikeRepository.existsPostLikeByPostIdAndUserId(likedPostId, userId);

		if(!exists) {
			postRepository.findById(likedPostId)
				.ifPresentOrElse(Post::decreaseLike,
					() -> log.error("Post Entity Does not exists By Id : {}", likedPostId)
				);
		} else {
			log.error("Unlike Post Event By postId:{}, userId:{}. PostLike Entity가 존재합니다.", likedPostId, userId);
		}
	}
}
