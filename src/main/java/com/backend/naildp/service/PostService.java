package com.backend.naildp.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;

	public List<HomePostResponse> homePosts(String nickname) {
		PageRequest pageRequest = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<Post> recentPosts = postRepository.findByBoundaryAndTempSaveFalse(Boundary.ALL, pageRequest);

		// 페이징된 게시물마다 해당 사용자가 저장 을 가져와야함
		// 1. archive 를 가져와서 archivePost에 있는지 확인
		// 2. archivepost - archive 를 가져와서 user에 해당하는게 있느지 확인

		// 페이징된 게시물마다 해당 사용자가 좋아요 를 가져와야함

		return null;
	}
}
