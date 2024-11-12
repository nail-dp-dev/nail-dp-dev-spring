package com.backend.naildp.service.post;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;

import lombok.RequiredArgsConstructor;

@Component("foryou")
@RequiredArgsConstructor
public class ForyouPostStrategy implements PostStrategy {

	private final PostRepository postRepository;
	private final TagPostRepository tagPostRepository;

	@Transactional(readOnly = true)
	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		List<Post> selectedPosts = new ArrayList<>(savedPostsInArchive);
		selectedPosts.addAll(likedPosts);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(selectedPosts);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSlice(username, cursorPostId, tagIdsInPosts,
			PageRequest.of(0, size));

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

}
