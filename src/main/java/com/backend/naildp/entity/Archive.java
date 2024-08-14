package com.backend.naildp.entity;

import static jakarta.persistence.FetchType.*;

import java.util.ArrayList;
import java.util.List;

import com.backend.naildp.common.Boundary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Boundary boundary;

	private String archiveImgUrl;

	@Builder
	public Archive(User user, String name, Boundary boundary, String archiveImgUrl) {
		this.user = user;
		this.name = name;
		this.boundary = boundary;
		this.archiveImgUrl = archiveImgUrl;
	}

	public Archive(User user, String name, Boundary boundary) {
		this.user = user;
		this.name = name;
		this.boundary = boundary;
	}

	public void updateImgUrl(String archiveImgUrl) {
		this.archiveImgUrl = archiveImgUrl;
	}

	public boolean notEqualsNickname(String nickname) {
		return !this.user.getNickname().equals(nickname);
	}

	public boolean isClosed() {
		return this.boundary == Boundary.NONE;
	}

	public boolean isOpenedForFollower() {
		return this.boundary == Boundary.FOLLOW;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateBoundary(Boundary boundary) {
		this.boundary = boundary;
	}

}
