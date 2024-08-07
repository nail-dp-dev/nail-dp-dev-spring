package com.backend.naildp.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@OneToMany(mappedBy = "comment")
	private List<CommentLike> commentLikes = new ArrayList<>();

	@Column(nullable = false)
	private String commentContent;

	@Formula("(select count(*) from comment_like where comment_like.comment_id = comment_id)")
	private long likeCount;

	public Comment(User user, Post post, String commentContent) {
		this.user = user;
		this.post = post;
		this.commentContent = commentContent;
	}

	public void modifyContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public boolean notRegisteredBy(String username) {
		return !user.equalsNickname(username);
	}
}
