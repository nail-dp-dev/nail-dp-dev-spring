package com.backend.naildp.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.comment.CommentInfoResponse;
import com.backend.naildp.dto.comment.CommentRegisterDto;
import com.backend.naildp.dto.comment.CommentSummaryResponse;
import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	@Transactional
	public Long registerComment(Long postId, CommentRegisterDto commentRegisterDto, String username) {
		Post post = postRepository.findPostAndUser(postId)
			.orElseThrow(() -> new CustomException("해당 포스트에 댓글을 등록할 수 없습니다. 다시 시도해주세요", ErrorCode.NOT_FOUND));
		User user = post.getUser();

		//post 에 댓글 달 수 있는지 확인
		//1. tempSave = false 인지
		//2. boundary 가 All 이거나 boundary 가 Follow 면서 username 이 팔로우 하고 있는지까지 확인 필요
		if (post.isTempSaved()) {
			throw new CustomException("임시저장한 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		if (post.isClosed() && post.notWrittenBy(username)) {
			throw new CustomException("비공개 게시물에는 댓글을 등록할 수 없습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(username, user)
			&& post.notWrittenBy(username)) {
			throw new CustomException("팔로워만 댓글을 등록할 수 있습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		//comment 생성 및 저장
		User commenter = userRepository.findByNickname(username)
			.orElseThrow(() -> new CustomException("다시 시도해주세요.", ErrorCode.NOT_FOUND));
		Comment comment = new Comment(commenter, post, commentRegisterDto.getCommentContent());
		post.addComment(comment);

		notificationService.generateCommentNotification(user, comment);

		return commentRepository.save(comment).getId();
	}

	@Transactional
	public Long modifyComment(Long postId, Long commentId, CommentRegisterDto commentModifyDto, String username) {
		Comment comment = commentRepository.findCommentAndPostAndUser(commentId)
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (comment.notRegisteredBy(username)) {
			throw new CustomException("댓글은 작성자만 수정할 수 있습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		comment.modifyContent(commentModifyDto.getCommentContent());

		return comment.getId();
	}

	@Transactional
	public void deleteComment(Long postId, Long commentId, String username) {
		Comment comment = commentRepository.findCommentAndPostAndUser(commentId)
			.orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (comment.notRegisteredBy(username)) {
			throw new CustomException("댓글은 작성자만 삭제할 수 있습니다.", ErrorCode.COMMENT_AUTHORITY);
		}

		Post post = comment.getPost();
		post.deleteComment(comment);

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
