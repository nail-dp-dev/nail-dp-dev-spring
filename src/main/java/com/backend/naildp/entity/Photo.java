package com.backend.naildp.entity;

import com.backend.naildp.dto.post.FileRequestDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "photo_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@Column(nullable = false)
	private String photoUrl;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long size;

	public Photo(Post post, String photoUrl, String name) {
		this.post = post;
		this.photoUrl = photoUrl;
		this.name = name;
	}

	public Photo(Post post, FileRequestDto fileRequestDto) {
		this.post = post;
		this.photoUrl = fileRequestDto.getFileUrl();
		this.name = fileRequestDto.getFileName();
		this.size = fileRequestDto.getFileSize();
	}
}
