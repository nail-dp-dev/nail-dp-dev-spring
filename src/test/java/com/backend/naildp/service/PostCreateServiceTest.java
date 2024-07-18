package com.backend.naildp.service;

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
		PostRequestDto postRequestDto = new PostRequestDto("postContent", false, Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			Collections.emptyList());

		List<MultipartFile> files = List.of(
			new MockMultipartFile("file1", new byte[] {1, 2, 3}),
			new MockMultipartFile("file2", new byte[] {4, 5, 6})
		);

		User user = new User(nickname, "010-1234-5678", 0L, UserRole.USER);

		List<FileRequestDto> fileRequestDtos = List.of(
			new FileRequestDto("file1", 12345L, "fileUrl1"),
			new FileRequestDto("file2", 12345L, "fileUrl2")
		);

		Post post = new Post(postRequestDto, user);

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

	@Test
	@DisplayName("게시물 수정 테스트")
	void testEditPost() {
		// given
		String nickname = "testUser";

		List<MultipartFile> files = List.of(
			new MockMultipartFile("newFile1", new byte[] {1, 2, 3}),
			new MockMultipartFile("newFile2", new byte[] {4, 5, 6})
		);

		User user = new User(nickname, "010-1234-5678", 0L, UserRole.USER);

		List<FileRequestDto> fileRequestDtos = List.of(
			new FileRequestDto("newFile1", 12345L, "newFileUrl1"),
			new FileRequestDto("newFile2", 12345L, "newFileUrl2")
		);

		PostRequestDto postRequestDto = new PostRequestDto("editContent", false, Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			List.of("fileUrl1", "fileUrl2"));

		Post post = new Post(postRequestDto, user);
		Long postId = 10L;
		post.setId(postId);
		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.findById(postId)).willReturn(Optional.of(post));
		given(tagRepository.findByName(anyString())).willAnswer(invocation -> {
			String tagName = invocation.getArgument(0);
			log.info(tagName);
			return Optional.empty();
		});
		given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(photoRepository.findByPhotoUrl(anyString())).willAnswer(
			invocation -> {
				String photoUrl = invocation.getArgument(0);
				log.info(photoUrl);
				return Optional.of(new Photo(post, new FileRequestDto(photoUrl, 12345L, photoUrl)));
			});

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
		verify(postRepository, times(1)).save(post);
		verify(s3Service, times(1)).saveFiles(files);
		verify(photoRepository, times(2)).save(any(Photo.class));
		verify(photoRepository, times(2)).delete(any(Photo.class));
		verify(s3Service, times(2)).deleteFile(anyString());

		for (String deletedFileUrl : postRequestDto.getDeletedFileUrls()) {
			verify(photoRepository).findByPhotoUrl(deletedFileUrl);
			log.info(deletedFileUrl);
			verify(photoRepository).delete(argThat(photo -> photo.getPhotoUrl().equals(deletedFileUrl)));
		}
	}

	@Test
	@DisplayName("게시물 수정 시 필수 조건 위반 예외")
	void testEditPost_fail() {
		// given
		String nickname = "testUser";
		Long postId = 10L;
		PostRequestDto postRequestDto = new PostRequestDto("editContent", false, Boundary.ALL,
			List.of(new TagRequestDto("tag1"), new TagRequestDto("tag2")),
			List.of("fileUrl1", "fileUrl2"));

		User user = new User(nickname, "010-1234-5678", 0L, UserRole.USER);

		Post post = new Post(postRequestDto, user);
		post.setId(postId);
		post.addPhoto(new Photo(post, new FileRequestDto("file1", 12345L, "fileUrl1")));
		post.addPhoto(new Photo(post, new FileRequestDto("file2", 12345L, "fileUrl2")));

		given(userRepository.findByNickname(nickname)).willReturn(Optional.of(user));
		given(postRepository.findById(postId)).willReturn(Optional.of(post));

		// then
		assertThrows(CustomException.class,
			() -> postService.editPost(nickname, postRequestDto, Collections.emptyList(), postId));
	}
}