package com.backend.naildp.service.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.service.FollowService;
import com.backend.naildp.service.post.dto.ForyouPostQuerySpec;
import com.backend.naildp.service.post.dto.PreferredPostDto;
import com.backend.naildp.service.post.dto.UserContext;
import com.backend.naildp.service.tag.TagReader;

import lombok.RequiredArgsConstructor;

@Component("foryou")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForyouPostStrategy implements PostStrategy {

	private final PostReader postReader;
	private final TagReader tagReader;
	private final FollowService followService;
	private final PostRepository postRepository;

	@Override
	public boolean isExecutable(UserContext userContext) {
		return requiresAuthentication() && userContext.isAuthenticated();
	}

	@Override
	public boolean requiresAuthentication() {
		return true;
	}

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postReader.getSavedPosts(username);
		List<Post> likedPosts = postReader.getLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagReader.getTagIdsInPosts(selectedPosts);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSlice(username, cursorPostId, tagIdsInPosts,
			PageRequest.of(0, size));

		return PostSummaryResponse.of(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV2(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postReader.getSavedPosts(username);
		List<Post> likedPosts = postReader.getLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagReader.getTagIdsInPosts(selectedPosts);

		Post cursorPost = postReader.findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV2(username, cursorPost, tagIdsInPosts,
			PageRequest.of(0, size));

		return PostSummaryResponse.of(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsWithoutTagPostJoin(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postReader.getSavedPosts(username);
		List<Post> likedPosts = postReader.getLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagReader.getTagIdsInPosts(selectedPosts);

		Post cursorPost = postReader.findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV2WithoutTagPostJoin(username, cursorPost, tagIdsInPosts,
			PageRequest.of(0, size));

		return PostSummaryResponse.of(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsFilteredByTagsAndFollowees(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postReader.getSavedPosts(username);
		List<Post> likedPosts = postReader.getLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagReader.getTagIdsInPosts(selectedPosts);

		List<User> readableUsers = followService.findReadableUsers(username);

		Post cursorPost = postReader.findCursorPost(cursorPostId);

		Slice<Post> forYouPostSlice =
			postRepository.findForYouPostSliceV2WithoutTagPostJoinAndFollowJoin(username, cursorPost, tagIdsInPosts, readableUsers,
			PageRequest.of(0, size));

		return PostSummaryResponse.of(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

	@Override
	public PostSummaryResponse homePostsV3(int size, Long cursorPostId, UserContext userContext) {
		String username = userContext.getUsername();
		PreferredPostDto preferredPostDto = postReader.getPreferredPostIds(username);

		Set<Long> preferredPostIds = preferredPostDto.all();

		List<Long> tagIdsInPosts = tagReader.getTagIdsInPostIds(preferredPostIds);

		List<UUID> readableUserIds = followService.findReadableUserIds(username);

		Post cursorPost = postReader.findCursorPost(cursorPostId);

		ForyouPostQuerySpec querySpec = ForyouPostQuerySpec.of(cursorPost, tagIdsInPosts, readableUserIds);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSliceV3(username, querySpec,
			PageRequest.of(0, size));

		return PostSummaryResponse.of(forYouPostSlice, preferredPostDto);
	}
}
