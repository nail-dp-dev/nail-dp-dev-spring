package com.backend.naildp.service.handler;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeUpdater {

	private final PostLikeRepository postLikeRepository;
	private final PostRepository postRepository;

	@Transactional
	public void increaseLikeCount(Long likedPostId, UUID userId) {
		postLikeRepository.findPostLikeByPostIdAndUserId(likedPostId, userId)
			.ifPresentOrElse(postLike -> {
					Post post = postLike.getPost();
					post.increaseLike();
					log.info("likeCount increase: {}", post.getTodayLikeCount());
				},
				() -> log.error("Like Post Event By postId:{}, userId:{}. PostLike Entity가 존재하지 않습니다.", likedPostId, userId)
			);
	}

	@Transactional
	public void decreaseLikeCount(Long likedPostId, UUID userId) {
		boolean exists = postLikeRepository.existsPostLikeByPostIdAndUserId(likedPostId, userId);

		if (exists) {
			log.error("Unlike Post Event By postId:{}, userId:{}. PostLike Entity가 존재합니다.", likedPostId, userId);
		} else {
			postRepository.findById(likedPostId)
				.ifPresentOrElse(Post::decreaseLike,
					() -> log.error("Post Entity Does not exists By Id : {}", likedPostId));
		}
	}
}
