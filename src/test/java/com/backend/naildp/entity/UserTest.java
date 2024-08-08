package com.backend.naildp.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.backend.naildp.common.UserRole;

class UserTest {

	@Test
	void create() {

		User user = User.builder()
			.nickname("nick")
			.phoneNumber("pn")
			.role(UserRole.USER)
			.agreement(true)
			.build();

		assertThat(user.getThumbnailUrl()).isEqualTo("default");
		assertThat(user.getPoint()).isEqualTo(0L);
	}

}