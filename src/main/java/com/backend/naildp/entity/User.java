package com.backend.naildp.entity;

import java.util.UUID;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.LoginRequestDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID id;

	// @OneToMany(mappedBy = "users", fetch = FetchType.LAZY)
	// private List<SocialLogin> socialLoginList;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String phoneNumber;
	private String profileUrl;
	private Long point;
	private UserRole role;

	public User(String nickname, String phoneNumber, String profileUrl,
		Long point, UserRole role) {
		this.nickname = nickname;
		this.phoneNumber = phoneNumber;
		this.profileUrl = profileUrl;
		this.point = point;
		this.role = role;
	}

	public User(LoginRequestDto loginRequestDto, UserRole role) {
		this.nickname = loginRequestDto.getNickname();
		this.phoneNumber = loginRequestDto.getPhone_number();
		this.profileUrl = loginRequestDto.getProfile_url();
		this.role = role;
	}
}
