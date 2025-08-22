package com.backend.naildp.service.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Follow;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Component("foryou")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForyouPostStrategy implements PostStrategy {

	private final PostRepository postRepository;
	private final TagPostRepository tagPostRepository;
	private final FollowRepository followRepository;
	private final UserRepository userRepository;

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSlice(username, cursorPostId, tagIdsInPosts,
			PageRequest.of(0, size));

		if (forYouPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		Post cursorPost = findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV2(username, cursorPost, tagIdsInPosts,
			PageRequest.of(0, size));

		if (forYouPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		Post cursorPost = findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV2WithoutTagPostJoin(username, cursorPost, tagIdsInPosts,
			PageRequest.of(0, size));

		if (forYouPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoinAndFollow(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		// 요청한 사용자가 팔로우한 사용자에 게시물 작성자가 포함되는지 확인하는 로직이 필요
		// 자신이 작성한 게시글도 조회할 수 있으므로 포함
		User reader = userRepository.findByNickname(username)
			.orElseThrow(() -> new EntityNotFoundException("User Entity not found By nickname : " + username));
		List<Follow> followsByReader = followRepository.findFollowsByFollower(reader);
		List<User> followees = followsByReader.stream()
			.map(Follow::getFollowing)
			.collect(Collectors.toList());
		List<User> readableUsers = new ArrayList<>(followees);
		readableUsers.add(reader);

		Post cursorPost = findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice =
			postRepository.findForYouPostSliceV2WithoutTagPostJoinAndFollowJoin(username, cursorPost, tagIdsInPosts, readableUsers,
			PageRequest.of(0, size));

		if (forYouPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV3(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		Post cursorPost = findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV2(username, cursorPost, tagIdsInPosts,
			PageRequest.of(0, size));

		if (forYouPostSlice.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Nullable
	private Post findCursorPost(Long cursorPostId) {
		return cursorPostId == null ? null : postRepository.findById(cursorPostId).orElse(null);
	}
}
