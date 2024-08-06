package com.backend.naildp.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TempPostRequestDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	// @Builder.Default
	@OneToMany(mappedBy = "post")
	private List<Photo> photos = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "post")
	private List<Comment> comments = new ArrayList<>();

	// @Builder.Default
	@OneToMany(mappedBy = "post")
	private List<PostLike> postLikes = new ArrayList<>();

	// @Builder.Default
	@OneToMany(mappedBy = "post")
	private List<TagPost> tagPosts = new ArrayList<>();

	private String postContent;

	@Builder.Default
	private Long sharing = 0L;

	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false)
	private Boundary boundary; // FOLLOW, ALL, NONE

	@Column(nullable = false)
	private Boolean tempSave;

	public Post(User user, String postContent, Long sharing, Boundary boundary, Boolean tempSave) {
		this.user = user;
		this.postContent = postContent;
		this.sharing = sharing;
		this.boundary = boundary;
		this.tempSave = tempSave;
	}

	public void update(PostRequestDto postRequestDto) {
		this.postContent = postRequestDto.getPostContent();
		this.boundary = postRequestDto.getBoundary();
		this.tempSave = false;
	}

	public void tempUpdate(TempPostRequestDto tempPostRequestDto) {
		this.postContent = tempPostRequestDto.getPostContent();
		this.boundary = tempPostRequestDto.getBoundary();
		this.tempSave = true;
	}

	public void addPhoto(Photo photo) {
		this.photos.add(photo);
	}

	public void addTagPost(TagPost tagPost) {
		this.tagPosts.add(tagPost);
	}

	public void addPostLike(PostLike postLike) {
		this.postLikes.add(postLike);
	}

	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public boolean isTempSaved() {
		return this.tempSave;
	}

	public boolean isClosed() {
		return this.boundary == Boundary.NONE;
	}

	public boolean isOpenedForFollower() {
		return this.boundary == Boundary.FOLLOW;
	}

	public void deleteComment(Comment comment) {
		this.comments.remove(comment);
	}
}
