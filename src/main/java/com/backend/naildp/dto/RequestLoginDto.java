package com.backend.naildp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestLoginDto {
	private String nickname;
	private String phone_number;
	private String profile_url;
}
