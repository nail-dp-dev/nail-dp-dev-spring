package com.backend.naildp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostMapping;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final FollowRepository followRepository;
	private final PostLikeRepository postLikeRepository;

	public PostSummaryResponse getUserPosts(String myNickname, String postNickname, int size, long cursorPostId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(myNickname);
		if (cursorPostId == -1) {
			postList = postRepository.findUserPostsByFollow(myNickname, followingNickname, pageRequest);

			// 본인 게시물이면, 임시저장도 같이 보내기
			if (myNickname.equals(postNickname)) {
				Optional<Post> tempPostOptional = postRepository.findPostByTempSaveIsTrueAndUserNickname(myNickname);
				if (tempPostOptional.isPresent()) {
					Post tempPost = tempPostOptional.get();
					// postList의 내용물을 리스트로 변환하여 임시 포스트를 추가하고, 다시 Slice로 변환
					List<Post> updatedPosts = new ArrayList<>(postList.getContent());
					updatedPosts.add(0, tempPost);
					postList = new SliceImpl<>(updatedPosts, pageRequest, false);
				}
			}

		} else {
			postList = postRepository.findUserPostsByIdAndFollow(cursorPostId, myNickname, followingNickname,
				pageRequest);

		}

		List<PostMapping> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(myNickname);
		List<PostMapping> likedPosts = postLikeRepository.findPostLikesByUserNickname(myNickname);

		return new PostSummaryResponse(postList, savedPosts, likedPosts);

	}
}
