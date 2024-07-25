package com.backend.naildp.dto.auth;

import com.backend.naildp.validation.ValidationGroups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NicknameRequestDto {
	@NotBlank(message = "닉네임을 입력해주세요", groups = ValidationGroups.NotEmptyGroup.class)
	@Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{4,15}$", message = "닉네임은 특수문자를 제외한 4~15자리여야 합니다.", groups = ValidationGroups.PatternCheckGroup.class)
	private String nickname;

}
