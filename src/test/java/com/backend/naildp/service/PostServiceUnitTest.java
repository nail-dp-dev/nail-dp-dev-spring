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
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
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
	FollowRepository followRepository;

	@Mock
	AuditingHandler auditingHandler;

	final static String NICKNAME = "mj";
	final static int POST_CNT = 20;
	final static int PAGE_NUMBER = 0;
	final static int PAGE_SIZE = 20;

	@DisplayName("최신 게시물 조회 단위 테스트 - 첫 호출")
	@Test
	void newPosts() {
		//given
		long cursorPostId = -1L;
		PageRequest pageRequest = createPageRequest(PAGE_NUMBER, PAGE_SIZE, "id");

		List<User> followingUser = new ArrayList<>();
		List<Post> posts = createTestPosts(POST_CNT);
		Slice<Post> recentPosts = new SliceImpl<>(posts, pageRequest, true);
		List<ArchivePost> archivePosts = savePostsInArchive(recentPosts);
		List<PostLike> postLikes = likePosts(recentPosts);

		when(followRepository.findFollowingUserByFollowerNickname(anyString())).thenReturn(followingUser);
		when(postRepository.findRecentPostsByFollowing(anyList(), eq(pageRequest)))
			.thenReturn(recentPosts);
		when(archivePostRepository.findAllByArchiveUserNickname(eq(NICKNAME))).thenReturn(archivePosts);
		when(postLikeRepository.findAllByUserNickname(eq(NICKNAME))).thenReturn(postLikes);

		//when
		PostSummaryResponse postSummaryResponse = postService.homePosts("NEW", PAGE_SIZE,
			cursorPostId, NICKNAME);
		Slice<HomePostResponse> homePostResponses = postSummaryResponse.getPostSummaryList();

		//then
		verify(followRepository).findFollowingUserByFollowerNickname(anyString());
		verify(postRepository).findRecentPostsByFollowing(anyList(), eq(pageRequest));
		verify(postRepository, never()).findRecentPostsByIdAndFollowing(anyLong(),
			anyList(), any(PageRequest.class));
		verify(archivePostRepository).findAllByArchiveUserNickname(NICKNAME);
		verify(postLikeRepository).findAllByUserNickname(NICKNAME);

		assertThat(homePostResponses.getNumber()).isEqualTo(0);
		assertThat(homePostResponses.getSize()).isEqualTo(20);
		assertThat(homePostResponses.isFirst()).isTrue();
		assertThat(homePostResponses.hasNext()).isTrue();
	}

	@DisplayName("최신 게시물 조회 단위 테스트 - 두번쨰 호출부터")
	@Test
	void newPostsWithCursorId() {
		//given
		long cursorPostId = 10L;
		PageRequest pageRequest = createPageRequest(PAGE_NUMBER, PAGE_SIZE, "id");

		List<User> followingUser = new ArrayList<>();
		List<Post> posts = createTestPosts(POST_CNT);
		Slice<Post> pagedPost = new SliceImpl<>(posts, pageRequest, false);
		List<ArchivePost> archivePosts = savePostsInArchive(pagedPost);
		List<PostLike> postLikes = likePosts(pagedPost);

		when(followRepository.findFollowingUserByFollowerNickname(anyString())).thenReturn(followingUser);
		when(postRepository.findRecentPostsByIdAndFollowing(eq(cursorPostId), anyList(), eq(pageRequest)))
			.thenReturn(pagedPost);
		when(archivePostRepository.findAllByArchiveUserNickname(NICKNAME)).thenReturn(archivePosts);
		when(postLikeRepository.findAllByUserNickname(NICKNAME)).thenReturn(postLikes);

		//when
		PostSummaryResponse postSummaryResponse = postService.homePosts("NEW", PAGE_SIZE, cursorPostId, NICKNAME);
		Slice<HomePostResponse> homePostResponses = postSummaryResponse.getPostSummaryList();

		//then
		verify(followRepository).findFollowingUserByFollowerNickname(anyString());
		verify(postRepository).findRecentPostsByIdAndFollowing(eq(cursorPostId), anyList(), eq(pageRequest));
		verify(postRepository, never()).findRecentPostsByFollowing(anyList(), eq(pageRequest));
		verify(archivePostRepository).findAllByArchiveUserNickname(NICKNAME);
		verify(postLikeRepository).findAllByUserNickname(NICKNAME);

		assertThat(homePostResponses.getNumber()).isEqualTo(0);
		assertThat(homePostResponses.getSize()).isEqualTo(20);
		assertThat(homePostResponses.isFirst()).isTrue();
		assertThat(homePostResponses.hasNext()).isFalse();
	}

	@DisplayName("최신 게시물 조회 단위 테스트 - 게시물이 존재하지 않으면 예외 발생")
	@Test
	void newPostsException() {
		//given
		long cursorPostId = -1L;
		PageRequest pageRequest = createPageRequest(PAGE_NUMBER, PAGE_SIZE, "id");

		List<User> followingUser = new ArrayList<>();
		List<Post> posts = createTestPosts(0);
		Slice<Post> recentPosts = new SliceImpl<>(posts, pageRequest, true);

		when(followRepository.findFollowingUserByFollowerNickname(anyString())).thenReturn(followingUser);
		when(postRepository.findRecentPostsByFollowing(anyList(), eq(pageRequest)))
			.thenReturn(recentPosts);

		//when & then
		assertThatThrownBy(() -> postService.homePosts("NEW", PAGE_SIZE, cursorPostId, NICKNAME))
			.isInstanceOf(CustomException.class)
			.hasMessage("게시물이 없습니다.");

		verify(followRepository).findFollowingUserByFollowerNickname(anyString());
		verify(postRepository).findRecentPostsByFollowing(anyList(), eq(pageRequest));
		verify(archivePostRepository, never()).findAllByArchiveUserNickname(NICKNAME);
		verify(postLikeRepository, never()).findAllByUserNickname(NICKNAME);
	}

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
		PostSummaryResponse response = postService.findLikedPost(nickname, pageSize, -1);
		Slice<HomePostResponse> postSummaryList = response.getPostSummaryList();

		//then
		assertThat(postSummaryList.hasNext()).isFalse();
		assertThat(postSummaryList).extracting("like").containsOnly(true);

		verify(postLikeRepository).findPostLikesByFollowing(eq(nickname), anyList(), any(PageRequest.class));
		verify(postLikeRepository, never()).findPostLikesByIdAndFollowing(anyString(), anyLong(), anyList(),
			any(PageRequest.class));
	}

	@DisplayName("좋아요한 게시글 조회 예외 - 좋아요때게시글이 없을 때")
	@Test
	void noLikedPostsException() {
		//given
		String nickname = "mj";
		int pageSize = 20;
		PageRequest pageRequest = createPageRequest(0, pageSize, "id");
		List<User> followingUsers = new ArrayList<>();

		when(followRepository.findFollowingUserByFollowerNickname(eq(nickname))).thenReturn(followingUsers);
		when(postLikeRepository.findPostLikesByFollowing(eq(nickname), anyList(), any(PageRequest.class)))
			.thenThrow(new CustomException("좋아요한 게시물이 없습니다.", ErrorCode.FILES_NOT_REGISTERED));

		//when & then
		assertThatThrownBy(() -> postService.findLikedPost(nickname, pageSize, -1L))
			.isInstanceOf(CustomException.class)
			.hasMessage("좋아요한 게시물이 없습니다.");
	}

	@DisplayName("익명 사용자 - 최신 게시글 조회 테스트")
	@Test
	void recentPostsAccessByAnonymousUser() {
		//given
		long cursorId = -1L;
		String nickname = "";
		List<Post> posts = createTestPosts(POST_CNT);
		PageRequest pageRequest = createPageRequest(PAGE_NUMBER, PAGE_SIZE, "id");
		Slice<Post> pagedPost = new SliceImpl<>(posts, pageRequest, false);

		when(postRepository.findPostsByBoundaryAndTempSaveFalse(eq(Boundary.ALL), eq(pageRequest)))
			.thenReturn(pagedPost);

		//when
		PostSummaryResponse postSummaryResponse = postService.homePosts("NEW", PAGE_SIZE, cursorId, nickname);

		//then
		verify(postRepository).findPostsByBoundaryAndTempSaveFalse(Boundary.ALL, pageRequest);
		verify(postRepository, never())
			.findPostsByBoundaryNotAndTempSaveFalse(any(Boundary.class), any(PageRequest.class));
		verify(postRepository, never())
			.findPostsByIdBeforeAndBoundaryNotAndTempSaveIsFalse(anyLong(), any(Boundary.class),
				any(PageRequest.class));
		verify(archivePostRepository, never()).findAllByArchiveUserNickname(NICKNAME);
		verify(postLikeRepository, never()).findAllByUserNickname(NICKNAME);
	}

	@DisplayName("익명 사용자 - 최신 게시글 조회 예외 테스트")
	@Test
	void recentPostsExceptionByAnonymousUser() {
		//given
		long cursorId = -1L;
		String nickname = "";
		List<Post> posts = createTestPosts(0);
		PageRequest pageRequest = createPageRequest(0, PAGE_SIZE, "id");
		Slice<Post> postSlice = new SliceImpl<>(posts, pageRequest, false);

		when(postRepository.findPostsByBoundaryAndTempSaveFalse(eq(Boundary.ALL), eq(pageRequest)))
			.thenReturn(postSlice);

		//when & then
		assertThatThrownBy(() -> postService.homePosts("NEW", PAGE_SIZE, cursorId, nickname))
			.isInstanceOf(CustomException.class)
			.hasMessage("최신 게시물이 없습니다.")
			.extracting("errorCode").isEqualTo(ErrorCode.FILES_NOT_REGISTERED);
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