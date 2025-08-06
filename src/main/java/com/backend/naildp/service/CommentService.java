package com.backend.naildp.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.comment.CommentInfoResponse;
import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Notification;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final PostAccessValidator postAccessValidator;
	private final NotificationManager notificationManager;

	@Transactional
	public Long registerComment(Long postId, CommentRegisterDto commentRegisterDto, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		postAccessValidator.isAvailablePost(post, username);

		User commenter = userRepository.findByNickname(username)
			.orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
		Comment comment = Comment.of(commenter, post, commentRegisterDto.getCommentContent());
		post.addComment(comment);

		User postWriter = post.getUser();
		notificationManager.handleCommentNotification(comment, postWriter);

		return commentRepository.save(comment).getId();
	}

	@Transactional
	public Long modifyComment(Long postId, Long commentId, CommentRegisterDto commentModifyDto, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트에 댓글을 등록할 수 없습니다. 다시 시도해주세요", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		Comment comment = commentRepository.findCommentAndUser(commentId)
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (comment.notRegisteredBy(username)) {
			throw new CustomException("댓글은 작성자만 수정할 수 있습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		comment.modifyContent(commentModifyDto.getCommentContent());

		return comment.getId();
	}

	@Transactional
	public void deleteComment(Long postId, Long commentId, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트에 댓글을 등록할 수 없습니다. 다시 시도해주세요", ErrorCode.NOT_FOUND));

		postAccessValidator.isAvailablePost(post, username);

		Comment comment = commentRepository.findCommentAndPostAndUser(commentId)
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (comment.notRegisteredBy(username)) {
			throw new CustomException("댓글은 작성자만 삭제할 수 있습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		Post commentedPost = comment.getPost();
		commentedPost.deleteComment(comment);

		commentRepository.delete(comment);
	}

	public CommentSummaryResponse findComments(Long postId, int size, long cursorId, String nickname) {
		//좋아요수, 대댓글수, 작성시간 기준으로 정렬
		//v1은 좋아요수, 작성시간 기준
		PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "likeCount", "createdDate"));

		Slice<Comment> commentSlice;
		if (cursorId == -1L) {
			commentSlice = commentRepository.findCommentsByPostId(postId, pageRequest);
		} else {
			long likeCount = commentRepository.countLikesById(cursorId);
			commentSlice = commentRepository.findCommentsByPostIdAndIdBefore(postId, cursorId, likeCount, pageRequest);
		}

		if (commentSlice.isEmpty()) {
			return CommentSummaryResponse.createEmptyResponse();
		}

		//comment, user, commentLike
		Slice<CommentInfoResponse> commentInfoResponseSlice = commentSlice.map(
			comment -> CommentInfoResponse.of(comment, nickname));
		Long cursorCommentId = commentSlice.getContent().get(commentSlice.getNumberOfElements() - 1).getId();

		return new CommentSummaryResponse(cursorCommentId, commentInfoResponseSlice);
	}
}
