package com.backend.naildp.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;

	public List<SearchUserResponse> searchUsers(String nicknameKeyword, String username) {
		List<SearchUserResponse> searchUserResponses = userRepository.searchByKeyword(nicknameKeyword, username);
		return searchUserResponses;
	}

	public PostSummaryResponse searchPosts(Pageable pageable, String postKeyword, String username, Long cursor) {
		Slice<Post> posts = postRepository.searchPostByKeyword(pageable, postKeyword, username, cursor);

		if (posts.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> postsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		return new PostSummaryResponse(posts, postsInArchive, likedPosts);
	}

}
