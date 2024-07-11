package com.backend.naildp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final PostLikeRepository postLikeRepository;

	public Page<HomePostResponse> homePosts(String choice, int pageNumber, String nickname) {
		PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<Post> recentPosts = postRepository.findPostsAndPhotoByBoundary(Boundary.ALL, pageRequest);

		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		List<PostLike> postLikes = postLikeRepository.findAllByUserNickname(nickname);
		List<Post> likedPosts = postLikes.stream().map(PostLike::getPost).collect(Collectors.toList());

		return recentPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts));
	}

	public Page<HomePostResponse> findLikedPost(String nickname, int pageNumber) {
		PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "createdDate"));

		// 좋아요한 게시글 조회
		Page<PostLike> postLikes = postLikeRepository.findPagedPostLikesByBoundaryOpened(pageRequest, nickname,
			Boundary.NONE);
		Page<Post> likedPost = postLikes.map(PostLike::getPost);

		// 게시글 저장 여부 체크
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		return likedPost.map(post -> HomePostResponse.likedPostResponse(post, savedPosts));
	}
}
