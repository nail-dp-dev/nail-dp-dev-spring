package com.backend.naildp.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;

class PostTest {

	@Test
	void create() {
		User user = User.builder()
			.nickname("nickname")
			.phoneNumber("pn")
			.role(UserRole.USER)
			.agreement(true)
			.build();

		Post post = Post.builder()
			.user(user)
			.postContent("content")
			.boundary(Boundary.ALL)
			.tempSave(false)
			.build();

		assertThat(post.getId()).isNull();

		assertThat(post.getPhotos()).hasSize(0);
		assertThat(post.getComments()).hasSize(0);
		assertThat(post.getPostLikes()).hasSize(0);
		assertThat(post.getTagPosts()).hasSize(0);

		assertThat(post.getSharing()).isEqualTo(0L);
		assertThat(post.getSharing()).isNotNull();

		assertThat(post.getPostContent()).isEqualTo("content");

		assertThat(post.getBoundary()).isEqualTo(Boundary.ALL);
		assertThat(post.getTempSave()).isFalse();
	}

}