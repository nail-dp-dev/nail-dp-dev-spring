package com.backend.naildp.entity;

import java.util.UUID;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.auth.LoginRequestDto;

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

@Entity(name = "Users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

	@Builder.Default
	private Long point = 0L;

	@Enumerated(value = EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	private boolean agreement;

	@Column(nullable = false)
	private String thumbnailUrl;

	public User(String nickname, String phoneNumber,
		Long point, UserRole role) {
		this.nickname = nickname;
		this.phoneNumber = phoneNumber;
		this.point = point;
		this.role = role;
	}

	public User(LoginRequestDto loginRequestDto, UserRole role) {
		this.nickname = loginRequestDto.getNickname();
		this.phoneNumber = loginRequestDto.getPhoneNumber();
		this.agreement = loginRequestDto.isAgreement();
		this.role = role;
	}

	public boolean equalsNickname(String nickname) {
		return this.nickname.equals(nickname);
	}

	public void thumbnailUrlUpdate(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

}
