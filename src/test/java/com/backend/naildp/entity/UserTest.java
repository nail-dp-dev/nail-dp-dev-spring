package com.backend.naildp.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.backend.naildp.common.UserRole;

class UserTest {

	@Test
	void userCreateTest() {

		User user = User.builder()
			.nickname("nick")
			.phoneNumber("pn")
			.role(UserRole.USER)
			.agreement(true)
			.build();

		assertThat(user.getPoint()).isEqualTo(0L);
	}

}