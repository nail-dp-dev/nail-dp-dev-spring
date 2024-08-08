package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.post.EditPostResponseDto;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.dto.post.PostRequestDto;
import com.backend.naildp.dto.post.TagRequestDto;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
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

	@InjectMocks
	PostService postService;

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
}