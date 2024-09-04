package com.backend.naildp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.search.RelatedTagResponse;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.TagPostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final TagPostRepository tagPostRepository;

	public List<SearchUserResponse> searchUsers(String nicknameKeyword, String username) {
		List<SearchUserResponse> searchUserResponses = userRepository.searchByKeyword(nicknameKeyword, username);
		return searchUserResponses;
	}

	public PostSummaryResponse searchPosts(Pageable pageable, String postKeyword, String username, Long cursorId) {
		Slice<Post> posts = postRepository.searchPostByKeyword(pageable, postKeyword, username, cursorId);

		if (posts.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> postsInArchive = postRepository.findPostsInArchive(username);
		List<Post> likedPosts = postRepository.findLikedPosts(username);

		return new PostSummaryResponse(posts, postsInArchive, likedPosts);
	}

	public List<RelatedTagResponse> searchRelatedTagsByKeyword(String keyword, String username) {
		List<TagPost> tagPosts = tagPostRepository.searchRelatedTags(keyword.strip().toLowerCase(), username);

		Map<Tag, List<Photo>> tagPostMap = tagPosts.stream()
			.collect(Collectors.groupingBy(TagPost::getTag,
					LinkedHashMap::new,
					Collectors.collectingAndThen(
						Collectors.mapping(TagPost::getPost, Collectors.toList()),
						posts -> {
							Optional<Post> optionalPost = posts.stream()
								.filter(p -> p.getPhotos().size() == 1)
								.max(Comparator.comparingLong(p -> p.getPostLikes().size()));
							if (optionalPost.isPresent()) {
								return optionalPost.get().getPhotos();
							}
							return new ArrayList<>();
						}
					)
				)
			);

		return tagPostMap.entrySet().stream()
			.map(entry -> new RelatedTagResponse(entry.getKey(), entry.getValue()))
			.limit(10)
			.toList();
	}
}
