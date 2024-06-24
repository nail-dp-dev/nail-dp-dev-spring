package com.backend.naildp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final PostLikeRepository postLikeRepository;

	public List<HomePostResponse> homePosts(String nickname) {
		PageRequest pageRequest = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<Post> recentPosts = postRepository.findByBoundaryAndTempSaveFalse(Boundary.ALL, pageRequest);

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());

		return recentPosts.stream()
			.map(post -> new HomePostResponse(post, savedPosts, likedPosts))
			.collect(Collectors.toList());
	}
}
