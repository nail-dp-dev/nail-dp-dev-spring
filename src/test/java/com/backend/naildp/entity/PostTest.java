package com.backend.naildp.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.PostBoundaryRequest;

class PostTest {

	@Test
	void create() {
		User user = createUser("nickname");

		Post post = createPost(user);

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

	@DisplayName("게시글 작성자와 닉네임이 같은지 확인 테스트")
	@Test
	void checkPostWriterAndNickname() {
		//given
		String userNickname = "nickname";
		String wrongNickname = "wrongNickname";
		User user = createUser(userNickname);
		Post post = createPost(user);

		//when
		boolean notWrittenByUser = post.notWrittenBy(userNickname);
		boolean notWrittenByWrongUser = post.notWrittenBy(wrongNickname);

		//then
		assertThat(notWrittenByUser).isFalse();
		assertThat(notWrittenByWrongUser).isTrue();
	}

	@DisplayName("게시글 공개범위 설정")
	@ParameterizedTest
	@EnumSource(value = Boundary.class)
	void changeBoundary(Boundary boundary) {
		//given
		PostBoundaryRequest postBoundaryRequest = new PostBoundaryRequest(boundary);
		User user = createUser("nickname");
		Post post = createPost(user);

		//when
		post.changeBoundary(postBoundaryRequest);

		//then
		assertThat(post.getBoundary()).isEqualTo(postBoundaryRequest.getCloser());
	}

	private User createUser(String nickname) {
		return User.builder()
			.nickname(nickname)
			.phoneNumber("pn")
			.role(UserRole.USER)
			.agreement(true)
			.build();
	}

	private Post createPost(User user) {
		return Post.builder()
			.user(user)
			.postContent("content")
			.boundary(Boundary.ALL)
			.tempSave(false)
			.build();
	}

}