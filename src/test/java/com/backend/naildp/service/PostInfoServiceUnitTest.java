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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.config.JpaAuditingConfiguration;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.service.post.PostInfoService;

@ExtendWith(MockitoExtension.class)
@Import(JpaAuditingConfiguration.class)
class PostInfoServiceUnitTest {

	@InjectMocks
	PostInfoService postInfoService;

	@Mock
	PostRepository postRepository;

	@Mock
	ArchivePostRepository archivePostRepository;

	@Mock
	PostLikeRepository postLikeRepository;

	@Mock
	FollowRepository followRepository;

	@Mock
	AuditingHandler auditingHandler;

	final static String NICKNAME = "mj";
	final static int POST_CNT = 20;
	final static int PAGE_NUMBER = 0;
	final static int PAGE_SIZE = 20;

	@DisplayName("좋아요한 게시글 조회 테스트")
	@Test
	void findPagedLikedPost() {
		//given
		String nickname = "mj";
		int postCnt = 20;
		int pageSize = 20;
		PageRequest pageRequest = createPageRequest(0, pageSize, "id");

		List<User> followingUsers = new ArrayList<>();
		List<PostLike> postLikes = createPostLikes(postCnt);
		Slice<PostLike> postLikeSlice = (SliceImpl<PostLike>)new SliceImpl<>(postLikes, pageRequest, false);
		List<ArchivePost> archivePosts = savePostsInArchive(postLikeSlice.map(PostLike::getPost));

		when(followRepository.findFollowingUserByFollowerNickname(eq(nickname))).thenReturn(followingUsers);
		when(postLikeRepository.findPostLikesByFollowing(eq(nickname), anyList(), any(PageRequest.class)))
			.thenReturn(postLikeSlice);
		when(archivePostRepository.findAllByArchiveUserNickname(eq(nickname))).thenReturn(archivePosts);

		//when
		PostSummaryResponse response = postInfoService.findLikedPost(nickname, pageSize, -1);
		Slice<HomePostResponse> postSummaryList = (Slice<HomePostResponse>)response.getPostSummaryList();

		//then
		assertThat(postSummaryList.hasNext()).isFalse();
		assertThat(postSummaryList).extracting("like").containsOnly(true);

		verify(postLikeRepository).findPostLikesByFollowing(eq(nickname), anyList(), any(PageRequest.class));
		verify(postLikeRepository, never()).findPostLikesByIdAndFollowing(anyString(), anyLong(), anyList(),
			any(PageRequest.class));
	}

	@DisplayName("좋아요한 게시글 조회 예외 - 좋아요때게시글이 없을 때")
	@Test
	void noLikedPostsTest() {
		//given
		String nickname = "mj";

		List<User> followingUsers = new ArrayList<>();

		int pageSize = 20;
		PageRequest pageRequest = createPageRequest(0, pageSize, "createdDate");
		Slice<PostLike> postLikeSlice = new SliceImpl<>(new ArrayList<>(), pageRequest, false);

		when(followRepository.findFollowingUserByFollowerNickname(eq(nickname))).thenReturn(followingUsers);
		when(postLikeRepository.findPostLikesByFollowing(eq(nickname), anyList(), eq(pageRequest)))
			.thenReturn(postLikeSlice);

		//when
		PostSummaryResponse response = postInfoService.findLikedPost(nickname, pageSize, -1L);
		Slice<HomePostResponse> postSummaryList = (Slice<HomePostResponse>)response.getPostSummaryList();

		//then
		assertThat(response).extracting(PostSummaryResponse::getCursorId).isEqualTo(-1L);
		assertThat(postSummaryList).hasSize(0);
		assertThat(postSummaryList.hasNext()).isFalse();
		assertThat(postSummaryList.getNumberOfElements()).isEqualTo(0);
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

	private List<PostLike> likePosts(Slice<Post> pagedPost) {
		return pagedPost.stream()
			.map(post -> new PostLike(null, post))
			.collect(Collectors.toList());
	}

	private List<ArchivePost> savePostsInArchive(Slice<Post> pagedPost) {
		return pagedPost.stream()
			.map(post -> new ArchivePost(null, post))
			.collect(Collectors.toList());
	}

	private PageRequest createPageRequest(int pageNumber, int pageSize, String property) {
		return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, property));
	}
}