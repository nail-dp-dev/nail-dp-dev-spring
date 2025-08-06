package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class PostAccessValidatorTest {

	@Mock
	FollowRepository followRepository;
	@InjectMocks
	PostAccessValidator postAccessValidator;

	@DisplayName("임시저장한 게시물은 댓글을 등록할 수 없다.")
	@Test
	void tempSavedPostThrowsException() {
		//given
		User user = createUser("user");
		Post tempSavedPost = createPost(user, Boundary.ALL, true);

		//when & then
		assertThatThrownBy(() -> postAccessValidator.isAvailablePost(tempSavedPost, user.getNickname()))
			.hasMessage("임시저장한 게시물에는 댓글을 등록할 수 없습니다.");
	}

	@DisplayName("비공개 게시물은 작성자만 접근할 수 있다.")
	@Test
	void privatePostThrowsException() {
		//given
		User user = createUser("user");
		Post privatePost = createPost(user, Boundary.NONE, false);

		//when & then
		assertThatThrownBy(() -> postAccessValidator.isAvailablePost(privatePost, "otherUser"))
			.hasMessage("비공개 게시물은 작성자만 접근할 수 있습니다.");
	}

	@DisplayName("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있다.")
	@Test
	void postForFollowerThrowsExceptionWhenNotFollowerAccess() {
		//given
		User user = createUser("user");
		Post postForFollower = createPost(user, Boundary.FOLLOW, false);

		//when
		when(followRepository.existsByFollowerNicknameAndFollowing(anyString(), eq(user))).thenReturn(false);

		//then
		assertThatThrownBy(() -> postAccessValidator.isAvailablePost(postForFollower, "notFollower"))
			.hasMessage("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.");
	}

	private User createUser(String nickname) {
		return User.builder()
			.nickname(nickname)
			.phoneNumber("")
			.thumbnailUrl("")
			.agreement(true)
			.role(UserRole.USER)
			.build();
	}

	private Post createPost(User user, Boundary boundary, boolean tempSave) {
		return Post.builder().user(user).postContent("").tempSave(tempSave).boundary(boundary).build();
	}
}