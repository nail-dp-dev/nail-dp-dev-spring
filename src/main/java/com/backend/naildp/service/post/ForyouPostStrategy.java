package com.backend.naildp.service.post;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Component("foryou")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ForyouPostStrategy implements PostStrategy {

	private final PostRepository postRepository;
	private final TagPostRepository tagPostRepository;

	@Override
	public PostSummaryResponse homePosts(int size, Long cursorPostId, String username) {
		List<Post> savedPostsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);
		likedPosts.addAll(savedPostsInArchive);

		List<Long> tagIdsInPosts = tagPostRepository.findTagIdsInPosts(likedPosts);

		Slice<Post> forYouPostSlice = postRepository.findForYouPostSlice(username, cursorPostId, tagIdsInPosts,
			PageRequest.of(0, size));

		return new PostSummaryResponse(forYouPostSlice, savedPostsInArchive, likedPosts);
	}

}
