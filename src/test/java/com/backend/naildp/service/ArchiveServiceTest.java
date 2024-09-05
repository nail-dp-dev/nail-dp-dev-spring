package com.backend.naildp.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.List;
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
import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.CreateArchiveRequestDto;
import com.backend.naildp.dto.archive.UnsaveRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostMapping;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ArchiveRepository archiveRepository;

	@Mock
	private ArchivePostRepository archivePostRepository;

	@Mock
	private PostRepository postRepository;

	@Mock
	private FollowRepository followRepository;

	@Mock
	private PostLikeRepository postLikeRepository;

	@InjectMocks
	private ArchiveService archiveService;

	private User user;
	private Archive archive;
	private Post post;

	private Photo photo;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.nickname("alswl")
			.agreement(true)
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.build();

		archive = new Archive(user, "Test Archive", Boundary.ALL);
		post = new Post(user, "Test Post", 0L, Boundary.ALL, false);
		photo = new Photo(post, "photoUrl.jpg", "photo.jpg");
		post.addPhoto(photo);
	}

	@DisplayName("아카이브 생성")
	@Test
	void createArchive_Success() {
		// Given
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));
		given(archiveRepository.countArchivesByUserNickname(anyString())).willReturn(0);

		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("New Archive", Boundary.ALL);

		// When
		archiveService.createArchive(user.getNickname(), requestDto);

		// Then
		then(archiveRepository).should(times(1)).save(any(Archive.class));
	}

	@DisplayName("아카이브생성 - 실패(NotFound)")
	@Test
	void createArchive_Notfound() {
		// Given
		given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("New Archive", Boundary.ALL);

		assertThatThrownBy(() -> archiveService.createArchive("nonexistent", requestDto))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("해당 유저가 존재하지 않습니다.");
	}

	@DisplayName("아카이브생성 - 실패(USER이고 4개 이상)")
	@Test
	void createArchive_UserAndLimit() {
		// Given
		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));
		given(archiveRepository.countArchivesByUserNickname(anyString())).willReturn(4);

		CreateArchiveRequestDto requestDto = new CreateArchiveRequestDto("New Archive", Boundary.ALL);

		assertThatThrownBy(() -> archiveService.createArchive(user.getNickname(), requestDto))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("더이상 아카이브를 생성할 수 없습니다.");
	}

	@DisplayName("아카이브에 게시물 저장")
	@Test
	void saveArchive_Success() {
		// Given
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(archive));
		given(archivePostRepository.existsByArchiveIdAndPostId(anyLong(), anyLong())).willReturn(false);

		// When
		archiveService.saveArchive(user.getNickname(), 1L, 1L);

		// Then
		then(archivePostRepository).should(times(1)).save(any(ArchivePost.class));
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(NotFoundPost)")
	@Test
	void saveArchive_NotFoundPost() {
		// Given
		given(postRepository.findById(anyLong())).willReturn(Optional.empty());

		assertThatThrownBy(() -> archiveService.saveArchive(user.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("게시물을 찾을 수 없습니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(NotFoundArchive)")
	@Test
	void saveArchive_NotFoundArchive() {
		// Given
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.empty());

		assertThatThrownBy(() -> archiveService.saveArchive(user.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("해당 아카이브를 찾을 수 없습니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(UserMismatch)")
	@Test
	void saveArchive_UserMismatch() {
		// Given
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(archive));

		assertThatThrownBy(() -> archiveService.saveArchive("nickname", 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("본인의 아카이브에만 접근할 수 있습니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(이미저장)")
	@Test
	void saveArchive_AlreadySave() {
		// Given
		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(archive));
		given(archivePostRepository.existsByArchiveIdAndPostId(anyLong(), anyLong())).willReturn(true);

		assertThatThrownBy(() -> archiveService.saveArchive(user.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("이미 저장한 게시물입니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(임시저장일경우)")
	@Test
	void saveArchive_IsTempSaved() {
		// Given
		post = new Post(user, "Test Post", 0L, Boundary.ALL, true);
		archive = new Archive(user, "Test Archive", Boundary.ALL);
		photo = new Photo(post, "photoUrl.jpg", "photo.jpg");
		post.addPhoto(photo);

		given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(archive));

		assertThatThrownBy(() -> archiveService.saveArchive(user.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("임시저장 게시물은 저장할 수 없습니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(비공개AndNotWritten)")
	@Test
	void saveArchive_IsClosed() {
		// Given
		User archiveUser = User.builder()
			.nickname("archiveUser")
			.agreement(true)
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.build();

		post = new Post(user, "Test Post", 0L, Boundary.NONE, false);
		archive = new Archive(archiveUser, "Test Archive", Boundary.ALL);
		photo = new Photo(post, "photoUrl.jpg", "photo.jpg");
		post.addPhoto(photo);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(1L)).willReturn(Optional.of(archive));

		assertThatThrownBy(() -> archiveService.saveArchive(archiveUser.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("비공개 게시물은 저장할 수 없습니다.");
	}

	@DisplayName("아카이브에 게시물 저장 - 실패(팔로워공개)")
	@Test
	void saveArchive_IsOpenedForFollowers() {
		// Given
		User archiveUser = User.builder()
			.nickname("archiveUser")
			.agreement(true)
			.phoneNumber("010-1234-5678")
			.role(UserRole.USER)
			.build();
		post = new Post(user, "Test Post", 0L, Boundary.FOLLOW, false);
		archive = new Archive(archiveUser, "Test Archive", Boundary.ALL);
		photo = new Photo(post, "photoUrl.jpg", "photo.jpg");
		post.addPhoto(photo);
		User postUser = post.getUser();

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(archiveRepository.findArchiveById(1L)).willReturn(Optional.of(archive));
		given(followRepository.existsByFollowerNicknameAndFollowing(archiveUser.getNickname(), postUser)).willReturn(
			false);

		assertThatThrownBy(() -> archiveService.saveArchive(archiveUser.getNickname(), 1L, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("팔로워만 게시물을 저장할 수 있습니다.");
	}

	@DisplayName("아카이브 복제")
	@Test
	void copyArchive_Success() {
		Archive originalArchive = Archive.builder()
			.name("Original Archive")
			.boundary(Boundary.ALL)
			.user(user)
			.archiveImgUrl("imageUrl")
			.build();

		ArchivePost archivePost = new ArchivePost(originalArchive, post);
		List<PostMapping> postList = Arrays.asList(() -> post);

		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(originalArchive));
		given(archivePostRepository.findArchivePostsByArchiveId(anyLong())).willReturn(postList);

		ArchiveIdRequestDto requestDto = new ArchiveIdRequestDto(1L);

		// When
		archiveService.copyArchive(user.getNickname(), requestDto);

		// Then
		then(archiveRepository).should().save(any(Archive.class));
		then(archivePostRepository).should().save(any(ArchivePost.class));
	}

	@Test
	void copyArchive_Success_NoPosts() {
		// Given
		Archive originalArchive = Archive.builder()
			.name("Original Archive")
			.boundary(Boundary.ALL)
			.user(user)
			.archiveImgUrl("imageUrl")
			.build();

		ArchivePost archivePost = new ArchivePost(originalArchive, post);

		given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(originalArchive));
		given(archivePostRepository.findArchivePostsByArchiveId(anyLong())).willReturn(Arrays.asList());

		ArchiveIdRequestDto requestDto = new ArchiveIdRequestDto(1L);

		// When
		archiveService.copyArchive(user.getNickname(), requestDto);

		// Then
		then(archiveRepository).should().save(any(Archive.class));
		then(archivePostRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void deleteArchive_Success() {
		// Given
		given(archiveRepository.findArchiveById(anyLong())).willReturn(Optional.of(archive));

		// When
		archiveService.deleteArchive(user.getNickname(), 1L);

		// Then
		then(archivePostRepository).should(times(1)).deleteAllByArchiveId(any());
		then(archiveRepository).should(times(1)).delete(any(Archive.class));
	}

	@Test
	@DisplayName("아카이브 이름 변경")
	void changeArchiveName() {
		// Given
		String nickname = "user1";
		Long archiveId = 1L;
		String newName = "New Archive Name";

		Archive archive = mock(Archive.class);
		given(archiveRepository.findArchiveById(archiveId)).willReturn(Optional.of(archive));
		given(archive.notEqualsNickname(nickname)).willReturn(false);

		// When
		archiveService.changeArchiveName(nickname, archiveId, newName);

		// Then
		verify(archive).updateName(newName);
	}

	@Test
	@DisplayName("아카이브 범위 변경")
	void changeArchiveBoundary() {
		// Given
		String nickname = "user1";
		Long archiveId = 1L;
		Boundary boundary = Boundary.FOLLOW;

		Archive archive = mock(Archive.class);
		given(archiveRepository.findArchiveById(archiveId)).willReturn(Optional.of(archive));
		given(archive.notEqualsNickname(nickname)).willReturn(false);

		// When
		archiveService.changeArchiveBoundary(nickname, archiveId, boundary);

		// Then
		verify(archive).updateBoundary(boundary);
	}

	@Test
	@DisplayName("아카이브에서 포스트 저장 해제")
	void unsaveFromArchive_Success() {
		// Given
		String nickname = "alswl";
		UnsaveRequestDto unsaveRequestDto = new UnsaveRequestDto(1L, List.of(111L, 112L));

		given(archiveRepository.findArchiveById(111L)).willReturn(Optional.of(archive));
		given(archiveRepository.findArchiveById(112L)).willReturn(Optional.of(archive));
		// When
		archiveService.unsaveFromArchive(nickname, unsaveRequestDto);

		// Then
		verify(archivePostRepository, times(1)).deleteAllByPostIdAndArchiveId(any(Long.class), anyList());
	}

	@Test
	@DisplayName("아카이브가 존재하지 않을 때 예외를 던진다")
	void unsaveFromArchive_ArchiveNotFound() {
		// Given
		String nickname = "alswl";
		UnsaveRequestDto unsaveRequestDto = new UnsaveRequestDto(1L, List.of(111L, 112L));

		given(archiveRepository.findArchiveById(111L)).willReturn(Optional.empty());

		// Then
		assertThatThrownBy(() -> archiveService.unsaveFromArchive(nickname, unsaveRequestDto))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("해당 아카이브를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("아카이브 소유자가 다를 때 예외를 던진다")
	void unsaveFromArchive_UserMismatch() {
		// Given
		String nickname = "user1";
		UnsaveRequestDto unsaveRequestDto = new UnsaveRequestDto(1L, List.of(111L, 112L));

		given(archiveRepository.findArchiveById(111L)).willReturn(Optional.of(archive));

		// Then
		assertThatThrownBy(() -> archiveService.unsaveFromArchive(nickname, unsaveRequestDto))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("본인의 아카이브에만 접근할 수 있습니다.");
	}

}