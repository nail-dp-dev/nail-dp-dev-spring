package com.backend.naildp.entity;

import static jakarta.persistence.FetchType.*;

import java.util.ArrayList;
import java.util.List;

import com.backend.naildp.common.Boundary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Archive extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "archive_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "archive")
	private List<ArchivePost> archivePosts = new ArrayList<>();

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Boundary boundary;

	private String archiveImgUrl;

	public static Archive of(User user, String name, Boundary boundary) {
		return Archive.builder()
			.user(user)
			.name(name)
			.boundary(boundary)
			.build();
	}

	public Archive(User user, String name, Boundary boundary) {
		this.user = user;
		this.name = name;
		this.boundary = boundary;
	}
}
