package com.backend.naildp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {
	@NotBlank(message = "닉네임을 입력해주세요")
	@Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{4,15}$", message = "닉네임은 특수문자를 제외한 4~15자리여야 합니다.")
	private String nickname;
	@NotBlank(message = "휴대폰 번호를 입력해주세요")
	private String phoneNumber;
	@NotNull(message = "약관 동의 여부를 입력해주세요")
	private boolean agreement;

	public LoginRequestDto(String nickname, String phoneNumber, boolean agreement) {
		this.nickname = nickname;
		this.phoneNumber = phoneNumber;
		this.agreement = agreement;
	}
}
