package com.backend.naildp.entity;

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
public class TagPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tag_post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id")
	private Tag tag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	public TagPost(Tag tag, Post post) {
		this.tag = tag;
		this.post = post;
	}

	public void update(Tag tag, Post post) {
		this.tag = tag;
		this.post = post;
	}
}
