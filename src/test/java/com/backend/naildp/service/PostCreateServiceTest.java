package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostBoundaryRequest;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TagRequestDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PhotoRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.TagRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PostCreateServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PostRepository postRepository;

	@Mock
	S3Service s3Service;

	@Mock
	TagRepository tagRepository;

	@Mock
	TagPostRepository tagPostRepository;

	@Mock
	PhotoRepository photoRepository;

	@Mock
	FollowRepository followRepository;

	@InjectMocks
	PostService postService;

	@Mock
	private PostDeletionFacade postDeletionFacade;

	@Test
	@DisplayName("게시물 업로드 테스트")
	void testUploadPost() {
		// given
		String nickname = "testUser";
		PostRequestDto postRequestDto = new PostRequestDto("postContent", Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			Collections.emptyList());

		List<MultipartFile> files = List.of(
			new MockMultipartFile("file1", new byte[] {1, 2, 3}),
			new MockMultipartFile("file2", new byte[] {4, 5, 6})
		);

		User user = createUser(nickname);

		List<FileRequestDto> fileRequestDtos = List.of(
			new FileRequestDto("file1", 12345L, "fileUrl1"),
			new FileRequestDto("file2", 12345L, "fileUrl2")
		);

		Post post = Post.builder()
			.user(user)
			.postContent(postRequestDto.getPostContent())
			.boundary(postRequestDto.getBoundary())
			.tempSave(false)
			.build();

		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.save(any(Post.class))).willReturn(post);
		given(s3Service.saveFiles(files)).willReturn(fileRequestDtos);
		given(tagRepository.findByName(anyString())).willAnswer(invocation -> {
			String tagName = invocation.getArgument(0);
			return Optional.empty();
		});
		given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(tagPostRepository.save(any(TagPost.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(photoRepository.save(any(Photo.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		postService.uploadPost(nickname, postRequestDto, files);

		// then
		verify(userRepository, times(1)).findByNickname(nickname);
		verify(postRepository, times(1)).save(any(Post.class));
		verify(s3Service, times(1)).saveFiles(files);
		verify(tagRepository, times(2)).findByName(anyString());
		verify(tagRepository, times(2)).save(any(Tag.class));
		verify(tagPostRepository, times(2)).save(any(TagPost.class));
		verify(photoRepository, times(2)).save(any(Photo.class));
	}

	private User createUser(String nickname) {
		return User.builder()
			.nickname(nickname)
			.phoneNumber("010-1234-5678")
			.agreement(true)
			.role(UserRole.USER)
			.build();
	}

	@Test
	@DisplayName("게시물 수정 테스트")
	void testEditPost() {
		// given
		String nickname = "testUser";
		Long postId = 10L;

		List<MultipartFile> files = List.of(
			new MockMultipartFile("newFile1", new byte[] {1, 2, 3}),
			new MockMultipartFile("newFile2", new byte[] {4, 5, 6})
		);

		User user = createUser(nickname);

		FileRequestDto fileRequestDto1 = new FileRequestDto("file1", 12345L, "fileUrl1");
		FileRequestDto fileRequestDto2 = new FileRequestDto("file2", 12345L, "fileUrl2");

		List<FileRequestDto> fileRequestDtos = List.of(
			fileRequestDto1, fileRequestDto2);

		PostRequestDto postRequestDto = new PostRequestDto("editContent", Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			List.of("fileUrl1", "fileUrl2"));

		Post post = Post.builder()
			.user(user)
			.postContent("content")
			.boundary(Boundary.FOLLOW)
			.tempSave(false)
			.build();

		Photo photo1 = new Photo(post, fileRequestDto1);
		Photo photo2 = new Photo(post, fileRequestDto2);

		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.findById(postId)).willReturn(Optional.of(post));
		given(tagRepository.findByName(anyString())).willAnswer(invocation -> {
			String tagName = invocation.getArgument(0);
			log.info(tagName);
			return Optional.empty();
		});
		given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(photoRepository.findByPhotoUrlIn(anyList())).willReturn(List.of(photo1, photo2));

		given(s3Service.saveFiles(files)).willReturn(fileRequestDtos);

		// when
		postService.editPost(nickname, postRequestDto, files, postId);

		// then
		verify(userRepository, times(1)).findByNickname(nickname);
		verify(postRepository, times(1)).findById(postId);
		verify(tagPostRepository, times(1)).deleteAllByPostId(postId);
		verify(tagRepository, times(2)).findByName(anyString());
		verify(tagRepository, times(2)).save(any(Tag.class));
		verify(tagPostRepository, times(2)).save(any(TagPost.class));
		verify(s3Service, times(1)).saveFiles(files);
		verify(photoRepository, times(2)).save(any(Photo.class));
		verify(photoRepository, times(2)).delete(any(Photo.class));
		verify(s3Service, times(2)).deleteFile(anyString());
		verify(photoRepository).findByPhotoUrlIn(postRequestDto.getDeletedFileUrls());

		assertEquals("editContent", post.getPostContent());
		assertEquals(Boundary.ALL, post.getBoundary());
		assertFalse(post.getTempSave());

	}

	@Test
	@DisplayName("게시물 수정 시 필수 조건 위반 예외")
	void testEditPost_fail() {
		// given
		String nickname = "testUser";
		Long postId = 10L;
		PostRequestDto postRequestDto = new PostRequestDto("editContent", Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			List.of("fileUrl1", "fileUrl2"));

		User user = createUser(nickname);

		Post post = Post.builder()
			.user(user)
			.postContent(postRequestDto.getPostContent())
			.boundary(postRequestDto.getBoundary())
			.tempSave(false)
			.build();

		post.addPhoto(new Photo(post, new FileRequestDto("file1", 12345L, "fileUrl1")));
		post.addPhoto(new Photo(post, new FileRequestDto("file2", 12345L, "fileUrl2")));

		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.findById(postId)).willReturn(Optional.of(post));

		// then
		CustomException exception = assertThrows(CustomException.class,
			() -> postService.editPost(nickname, postRequestDto, Collections.emptyList(), postId));

		assertThat(exception.getMessage()).isEqualTo("파일을 첨부해주세요.");

	}

	@Test
	@DisplayName("게시물 수정 조회 테스트")
	void testGetEditingPost() {
		// given
		String nickname = "testUser";
		Long postId = 10L;

		PostRequestDto postRequestDto = new PostRequestDto("postContent", Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			Collections.emptyList());

		User user = createUser(nickname);

		FileRequestDto fileRequestDto1 = new FileRequestDto("file1", 12345L, "fileUrl1");
		FileRequestDto fileRequestDto2 = new FileRequestDto("file2", 12345L, "fileUrl2");

		Post post = Post.builder()
			.user(user)
			.postContent(postRequestDto.getPostContent())
			.boundary(postRequestDto.getBoundary())
			.tempSave(false)
			.build();

		Photo photo1 = new Photo(post, fileRequestDto1);
		Photo photo2 = new Photo(post, fileRequestDto2);

		post.addPhoto(photo1);
		post.addPhoto(photo2);

		Tag tag1 = new Tag("tag1");
		Tag tag2 = new Tag("tag2");

		TagPost tagPost1 = new TagPost(tag1, post);
		TagPost tagPost2 = new TagPost(tag2, post);

		post.addTagPost(tagPost1);
		post.addTagPost(tagPost2);

		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.findById(postId)).willReturn(Optional.of(post));
		given(photoRepository.findAllByPostId(postId)).willReturn(List.of(photo1, photo2));

		// when
		EditPostResponseDto responseDto = postService.getEditingPost(nickname, postId);

		// then
		assertEquals("postContent", responseDto.getPostContent());
		assertEquals(2, responseDto.getTags().size());
		assertTrue(responseDto.getTags().contains("tag1"));
		assertTrue(responseDto.getTags().contains("tag2"));
		assertFalse(responseDto.getTags().contains("tag3"));
		assertEquals(2, responseDto.getPhotos().size());
		assertTrue(responseDto.getPhotos().stream().anyMatch(file -> file.getFileUrl().equals("fileUrl1")));
		assertTrue(responseDto.getPhotos().stream().anyMatch(file -> file.getFileUrl().equals("fileUrl2")));
		assertEquals(Boundary.ALL, responseDto.getBoundary());
		assertFalse(responseDto.getTempSave());
	}

	@DisplayName("게시물 공개범위 변경 예외 테스트 - 작성자와 요청자가 다를 때")
	@Test
	void changeBoundaryExceptionWithOtherUser() {
		//given
		String wrongUserNickname = "wrongNickname";
		User user = User.builder().nickname("nickname").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(user).postContent("content").tempSave(false).boundary(Boundary.ALL).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(post));

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> postService.changeBoundary(1L, new PostBoundaryRequest(Boundary.FOLLOW), wrongUserNickname));

		//then
		assertThat(exception.getMessage()).isEqualTo("게시글 범위 설정은 작성자만 할 수 있습니다.");
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_MISMATCH);
	}

	@DisplayName("게시물 공개범위 변경 테스트")
	@Test
	void changeBoundary() {
		//given
		String userNickname = "nickname";
		User user = User.builder().nickname(userNickname).phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(user).postContent("content").tempSave(false).boundary(Boundary.ALL).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(post));

		//when
		postService.changeBoundary(1L, new PostBoundaryRequest(Boundary.NONE), userNickname);

		//then
		verify(postRepository).findPostAndUser(1L);
	}

	@Test
	@DisplayName("게시물 삭제 성공 테스트")
	public void deletePostSuccessfully() {
		User user = User.builder().nickname("user1").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(user).postContent("content").tempSave(false).boundary(Boundary.ALL).build();
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		postService.deletePost(1L, "user1");

		then(postDeletionFacade).should().deletePostAndAssociations(1L);
	}

	@Test
	@DisplayName("게시물 삭제 실패 테스트 - 작성자가 아닌 경우")
	public void deletePost_IsNotAuthor() {
		User user = User.builder().nickname("user1").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(user).postContent("content").tempSave(false).boundary(Boundary.ALL).build();
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		assertThatThrownBy(() -> postService.deletePost(1L, "user2"))
			.isInstanceOf(CustomException.class)
			.hasMessage("게시글 삭제는 작성자만 할 수 있습니다.");

		then(postDeletionFacade).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("게시물 삭제 실패 테스트 - 게시물 존재하지 않는 경우")
	public void deletePost_NotFound() {
		User user = User.builder().nickname("user1").phoneNumber("pn").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(user).postContent("content").tempSave(false).boundary(Boundary.ALL).build();

		given(postRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> postService.deletePost(1L, "user1"))
			.isInstanceOf(CustomException.class)
			.hasMessage("해당 포스트를 찾을 수 없습니다.");

		then(postDeletionFacade).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("게시물 공유 횟수 조회 예외 - 비공개 게시물에 작성자가 아닌 경우")
	void privatePostSharedCountExceptionWithoutWriter() {
		//given
		User user = User.builder().nickname("user").phoneNumber("").agreement(true).role(UserRole.USER).build();
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post privatePost = Post.builder().user(writer).postContent("").tempSave(false).boundary(Boundary.NONE).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(privatePost));

		//when
		CustomException exception = assertThrows(CustomException.class, () -> postService.countSharing(1L, "user"));

		//then
		assertThat(exception).hasMessage("비공개 게시물은 작성자만 접근할 수 있습니다.");
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BOUNDARY);
	}

	@Test
	@DisplayName("게시물 공유 횟수 조회 예외 - 팔로우 공개 게시물에 작성자와 팔로워가 아닌 경우")
	void followPostSharedCountExceptionWithoutWriterAndFollower() {
		//given
		User user = User.builder().nickname("user").phoneNumber("").agreement(true).role(UserRole.USER).build();
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post followPost = Post.builder().user(writer).postContent("").tempSave(false).boundary(Boundary.FOLLOW).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		when(followRepository.existsByFollowerNicknameAndFollowing(eq(user.getNickname()), any(User.class)))
			.thenReturn(false);

		//when
		CustomException exception = assertThrows(CustomException.class, () -> postService.countSharing(1L, "user"));

		//then
		assertThat(exception).hasMessage("팔로우 공개 게시물은 팔로워와 작성자만 접근할 수 있습니다.");
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BOUNDARY);
	}

	@ParameterizedTest
	@DisplayName("게시물 공유 횟수 조회 - 팔로워가 조회")
	@EnumSource(value = Boundary.class, names = {"ALL", "FOLLOW"})
	void postSharedCountWithFollower(Boundary boundary) {
		//given
		User user = User.builder().nickname("user").phoneNumber("").agreement(true).role(UserRole.USER).build();
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post followPost = Post.builder().user(writer).postContent("").tempSave(false).boundary(boundary).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		lenient().when(followRepository.existsByFollowerNicknameAndFollowing(eq(user.getNickname()), any(User.class)))
			.thenReturn(true);

		//when
		postService.countSharing(1L, "user");

		//then
		assertThat(followPost.getSharing()).isEqualTo(0L);
	}

	@ParameterizedTest
	@DisplayName("게시물 공유 횟수 조회 - 작성자가 조회")
	@EnumSource(value = Boundary.class)
	void postSharedCountWithWriter(Boundary boundary) {
		//given
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post followPost = Post.builder().user(writer).postContent("").tempSave(false).boundary(boundary).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(followPost));
		lenient().when(followRepository.existsByFollowerNicknameAndFollowing(eq(writer.getNickname()), any(User.class)))
			.thenReturn(true);

		//when
		postService.countSharing(1L, "writer");

		//then
		assertThat(followPost.getSharing()).isEqualTo(0L);
	}

	@Test
	@DisplayName("임시저장 게시물 공유 예외")
	void tempSavedPostShareException() {
		//given
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post tempSavedPost = Post.builder().user(writer).postContent("").tempSave(true).boundary(Boundary.ALL).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(tempSavedPost));

		//when
		CustomException exception = assertThrows(CustomException.class, () -> postService.sharePost(1L, "user"));

		//then
		assertThat(exception).hasMessage("임시저장한 게시물은 공유할 수 없습니다.");
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
	}

	@ParameterizedTest
	@DisplayName("게시물 공유 테스트 - 작성자가 공유")
	@EnumSource(value = Boundary.class)
	void tempSavedPostShareException(Boundary boundary) {
		//given
		User writer = User.builder().nickname("writer").phoneNumber("").agreement(true).role(UserRole.USER).build();
		Post post = Post.builder().user(writer).postContent("").tempSave(false).boundary(boundary).build();

		when(postRepository.findPostAndUser(anyLong())).thenReturn(Optional.of(post));
		lenient().when(followRepository.existsByFollowerNicknameAndFollowing(eq(writer.getNickname()), any(User.class)))
			.thenReturn(true);

		//when
		postService.sharePost(1L, writer.getNickname());

		//then
		assertThat(post.getSharing()).isEqualTo(1);
	}
}