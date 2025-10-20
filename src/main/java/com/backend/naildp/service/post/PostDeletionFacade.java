package com.backend.naildp.service.post;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.repository.archive.ArchivePostRepository;
import com.backend.naildp.repository.comment.CommentRepository;
import com.backend.naildp.repository.post.PhotoRepository;
import com.backend.naildp.repository.post.PostLikeRepository;
import com.backend.naildp.repository.post.PostRepository;
import com.backend.naildp.repository.tag.TagPostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostDeletionFacade {
	private final ArchivePostRepository archivePostRepository;
	private final CommentRepository commentRepository;
	private final PhotoRepository photoRepository;
	private final PostLikeRepository postLikeRepository;
	private final TagPostRepository tagPostRepository;
	private final PostRepository postRepository;

	@Transactional
	public void deletePostAndAssociations(Long postId) {
		// 연관된 엔티티들 삭제
		archivePostRepository.deleteAllByPostId(postId);
		commentRepository.deleteAllByPostId(postId);
		photoRepository.deleteAllByPostId(postId);
		postLikeRepository.deleteAllByPostId(postId);
		tagPostRepository.deleteAllByPostId(postId);

		// Post 삭제
		postRepository.deleteById(postId);
	}
}
