package com.backend.naildp.entity;

import com.backend.naildp.common.ProfileType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Profile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "profile_id")
	private Long id;

	@Column(nullable = false)
	private String profileUrl;

	@Column(nullable = false)
	private Boolean thumbnail;

	@Column(nullable = false)
	private String name;

	@Enumerated(value = EnumType.STRING)
	@Column(nullable = false)
	private ProfileType profileType;

	public Profile(String profileUrl, String name, boolean thumbnail) {
		this.profileUrl = profileUrl;
		this.name = name;
		this.thumbnail = thumbnail;
	}
}
