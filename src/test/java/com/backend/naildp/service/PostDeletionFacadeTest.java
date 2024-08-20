package com.backend.naildp.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.CommentRepository;
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;

@ExtendWith(MockitoExtension.class)
public class PostDeletionFacadeTest {

	@InjectMocks
	private PostDeletionFacade postFacade;  // 실제 테스트할 대상 클래스

	@Mock
	private PostRepository postRepository;  // 의존성들에 대한 Mock 객체

	@Mock
	private ArchivePostRepository archivePostRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private PhotoRepository photoRepository;

	@Mock
	private PostLikeRepository postLikeRepository;

	@Mock
	private TagPostRepository tagPostRepository;

	private Post post;

	@BeforeEach
	public void setUp() {
		User user = createUser("user1");
		post = createPost(user, false, Boundary.ALL);
	}

	@Test
	@DisplayName("게시물과 연관된 엔티티를 성공적으로 삭제해야 한다")
	public void deletePostAndAssociations_Success() {
		postFacade.deletePostAndAssociations(1L);

		then(archivePostRepository).should().deleteAllByPostId(1L);
		then(commentRepository).should().deleteAllByPostId(1L);
		then(photoRepository).should().deleteAllByPostId(1L);
		then(postLikeRepository).should().deleteAllByPostId(1L);
		then(tagPostRepository).should().deleteAllByPostId(1L);
		then(postRepository).should().deleteById(1L);
	}

	@Test
	@DisplayName("사용자가 게시물 작성자가 아닌 경우 예외가 발생해야 한다")
	public void deletePostAndAssociations_NotAuthor() {
		assertThatThrownBy(() -> postFacade.deletePostAndAssociations(post.getId()))
			.isInstanceOf(CustomException.class);

		then(archivePostRepository).shouldHaveNoInteractions();
		then(commentRepository).shouldHaveNoInteractions();
		then(photoRepository).shouldHaveNoInteractions();
		then(postLikeRepository).shouldHaveNoInteractions();
		then(tagPostRepository).shouldHaveNoInteractions();
		then(postRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("게시물이 존재하지 않는 경우 예외가 발생해야 한다")
	public void deletePostAndAssociations_NotFound() {
		given(postRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> postFacade.deletePostAndAssociations(post.getId()))
			.isInstanceOf(CustomException.class);

		then(archivePostRepository).shouldHaveNoInteractions();
		then(commentRepository).shouldHaveNoInteractions();
		then(photoRepository).shouldHaveNoInteractions();
		then(postLikeRepository).shouldHaveNoInteractions();
		then(tagPostRepository).shouldHaveNoInteractions();
		then(postRepository).shouldHaveNoInteractions();
	}

	private User createUser(String nickname) {
		return User.builder().nickname(nickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
	}

	private Post createPost(User user, boolean tempSave, Boundary boundary) {
		return Post.builder().user(user).postContent("content").tempSave(tempSave).boundary(boundary).build();
	}

}