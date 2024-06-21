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
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;

	public List<HomePostResponse> homePosts(String nickname) {
		PageRequest pageRequest = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<Post> recentPosts = postRepository.findByBoundaryAndTempSaveFalse(Boundary.ALL, pageRequest);

		// 페이징된 게시물마다 해당 사용자가 저장 을 가져와야함
		List<ArchivePost> archivePosts = archivePostRepository.findAllByArchiveUserNickname(nickname);
		List<Post> savedPosts = archivePosts.stream()
			.map(ArchivePost::getPost)
			.collect(Collectors.toList());

		List<HomePostResponse> responses = recentPosts.stream()
			.map(post -> new HomePostResponse(post, savedPosts))
			.collect(Collectors.toList());

		// 페이징된 게시물마다 해당 사용자가 좋아요 를 가져와야함

		return null;
	}
}
