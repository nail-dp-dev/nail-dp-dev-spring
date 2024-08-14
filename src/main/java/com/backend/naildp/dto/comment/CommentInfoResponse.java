package com.backend.naildp.dto.comment;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.naildp.entity.Comment;
import com.backend.naildp.entity.CommentLike;
import com.backend.naildp.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentInfoResponse {

	private long commentId;
	private String commentContent;
	private String profileUrl;
	private String commentUserNickname;
	private LocalDateTime commentDate;
	private long likeCount;
	private boolean isLiked;
	private long replyCount;

	public static CommentInfoResponse of(Comment comment, String nickname) {
		List<CommentLike> commentLikes = comment.getCommentLikes();
		User commentUser = comment.getUser();

		return CommentInfoResponse.builder()
			.commentId(comment.getId())
			.commentContent(comment.getCommentContent())
			.profileUrl(commentUser.getThumbnailUrl())
			.commentUserNickname(commentUser.getNickname())
			.commentDate(comment.getCreatedDate())
			.likeCount(comment.getLikeCount())
			.isLiked(commentLikes.stream().anyMatch(commentLike -> commentLike.isLikedBy(nickname)))
			.replyCount(0)
			.build();
	}
}
