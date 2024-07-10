package com.backend.naildp.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
@Import(JpaAuditingConfiguration.class)
class PostServiceUnitTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostRepository postRepository;

	@Mock
	ArchivePostRepository archivePostRepository;

	@Mock
	PostLikeRepository postLikeRepository;

	@Mock
	AuditingHandler auditingHandler;

	@DisplayName("최신 게시물 조회 with 좋아요, 저장")
	@Test
	void test() {
		//given
		String nickname = "mj";
		int postCnt = 20;
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

		List<Post> posts = createTestPosts(postCnt);
		Page<Post> pagedPost = new PageImpl<>(posts, pageRequest, pageSize);
		List<ArchivePost> archivePosts = pagedPost.stream()
			.filter(post -> Integer.parseInt(post.getPostContent()) % 3 == 0)
			.map(post -> new ArchivePost(null, post))
			.collect(Collectors.toList());
		List<PostLike> postLikes = pagedPost.stream()
			.filter(post -> Integer.parseInt(post.getPostContent()) % 2 == 0)
			.map(post -> new PostLike(null, post))
			.collect(Collectors.toList());

		when(postRepository.findPostsAndPhotoByBoundary(Boundary.ALL, pageRequest)).thenReturn(pagedPost);
		when(archivePostRepository.findAllByArchiveUserNickname(nickname)).thenReturn(archivePosts);
		when(postLikeRepository.findAllByUserNickname(nickname)).thenReturn(postLikes);

		//when
		List<HomePostResponse> homePostResponses = postService.homePosts(nickname);
		List<HomePostResponse> likedPostResponses = homePostResponses.stream()
			.filter(HomePostResponse::getLike)
			.collect(Collectors.toList());
		List<HomePostResponse> savedPostResponses = homePostResponses.stream()
			.filter(HomePostResponse::getSaved)
			.collect(Collectors.toList());
		List<HomePostResponse> savedAndLikedPostResponses = homePostResponses.stream()
			.filter(HomePostResponse::getLike)
			.filter(HomePostResponse::getSaved)
			.collect(Collectors.toList());

		//then
		assertThat(homePostResponses.size()).isEqualTo(postCnt);
		assertThat(likedPostResponses.size()).isEqualTo(10);
		assertThat(savedPostResponses.size()).isEqualTo(7);
		assertThat(savedAndLikedPostResponses.size()).isEqualTo(4);

		// System.out.println("pagedPost 확인");
		// for (Post post : pagedPost) {
		// 	System.out.println("=================");
		// 	System.out.println("post.postContent() = " + post.getPostContent());
		// 	System.out.println("post.getCreatedDate() = " + post.getCreatedDate());
		// }
		// System.out.println("homeResponse 확인");
		// for (HomePostResponse response : homePostResponses) {
		// 	System.out.println("==================");
		// 	System.out.println("response.photoUrl() = " + response.getPhotoUrl());
		// 	System.out.println("response.like = " + response.getLike());
		// 	System.out.println("response.saved = " + response.getSaved());
		// }
	}

	@DisplayName("좋아요한 게시글 조회 테스트")
	@Test
	void findPagedLikedPost() {
		//given
		String nickname = "mj";
		int postCnt = 20;
		int pageSize = 20;
		PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdDate"));

		List<PostLike> postLikes = createPostLikes(postCnt);
		PageImpl<PostLike> pagedPostLikes = new PageImpl<>(postLikes, pageRequest, pageSize);
		Page<Post> pagedPosts = pagedPostLikes.map(PostLike::getPost);
		List<ArchivePost> archivePosts = pagedPosts.stream()
			.map(post -> new ArchivePost(null, post))
			.collect(Collectors.toList());

		when(postLikeRepository.findPagedPostLikesByBoundaryOpened(any(PageRequest.class), anyString(),
			any(Boundary.class))).thenReturn(pagedPostLikes);
		when(archivePostRepository.findAllByArchiveUserNickname(nickname)).thenReturn(archivePosts);

		//when
		Page<HomePostResponse> likedPostResponses = postService.findLikedPost(nickname, 0);
		Page<Boolean> savedList = likedPostResponses.map(HomePostResponse::getSaved);
		Page<Boolean> likedList = likedPostResponses.map(HomePostResponse::getLike);

		//then
		verify(postLikeRepository).findPagedPostLikesByBoundaryOpened(any(PageRequest.class), anyString(),
			any(Boundary.class));
		verify(archivePostRepository).findAllByArchiveUserNickname(nickname);
		assertThat(savedList.getTotalElements()).isEqualTo(20);
		assertThat(likedList.getTotalElements()).isEqualTo(20);
	}

	private List<Post> createTestPosts(int postCnt) {
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < postCnt; i++) {
			Post post = new Post(null, "" + i, 0L, Boundary.ALL, false);
			Photo photo = new Photo(post, "url" + i, "photo" + i);
			auditingHandler.markCreated(post);
			post.addPhoto(photo);
			posts.add(post);
		}
		return posts;
	}

	private List<PostLike> createPostLikes(int postCnt) {
		List<PostLike> postLikes = new ArrayList<>();
		List<Post> posts = createTestPosts(postCnt);
		posts.forEach(post -> {
			PostLike postLike = new PostLike(null, post);
			postLikes.add(postLike);
		});
		return postLikes;
	}
}