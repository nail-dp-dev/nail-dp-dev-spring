package com.backend.naildp.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.naildp.common.Boundary;

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
public class Post extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "post")
	private List<Photo> photos = new ArrayList<>();

	private String postContent;

	@Column(nullable = false)
	private Long sharing;

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

	public void addPhoto(Photo photo) {
		this.photos.add(photo);
	}
}
