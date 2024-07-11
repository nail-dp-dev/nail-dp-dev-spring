package com.backend.naildp.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.post.PostRequestDto;

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
	
	private Long sharing;

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

	public Post(PostRequestDto postRequestDto, User user) {
		this.user = user;
		this.postContent = postRequestDto.getPostContent();
		this.boundary = postRequestDto.getBoundary();
		this.tempSave = postRequestDto.getTempSave();
	}

	public void addPhoto(Photo photo) {
		this.photos.add(photo);
	}
}
