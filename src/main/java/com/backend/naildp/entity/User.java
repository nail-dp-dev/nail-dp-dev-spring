package com.backend.naildp.entity;

import java.util.UUID;

import org.hibernate.annotations.Formula;

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
import lombok.Builder;
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

	private String loginId;

	private String password;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String phoneNumber;

	private Long point = 0L;

	@Enumerated(value = EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	private boolean agreement;

	@Column(nullable = false)
	private String thumbnailUrl = "default";

	@Formula("(select count(*) from archive where archive.user_id = user_id and archive.boundary <> 'NONE' )")
	private int archiveCount;

	@Builder
	public User(String nickname, String phoneNumber, UserRole role, boolean agreement) {
		this.nickname = nickname;
		this.phoneNumber = phoneNumber;
		this.role = role;
		this.agreement = agreement;
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

	public void updatePoint(Long point) {
		this.point = point;
	}

	public void addAdminLoginId(String loginId) {
		this.loginId = loginId;
	}

	public void addAdminPassword(String password) {
		this.password = password;
	}

}
