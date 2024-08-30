package com.backend.naildp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.userInfo.TempSaveResponseDto;
import com.backend.naildp.entity.Post;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPostService {

	private final PostRepository postRepository;
	private final ArchivePostRepository archivePostRepository;
	private final FollowRepository followRepository;
	private final PostLikeRepository postLikeRepository;

	public PostSummaryResponse getUserPosts(String myNickname, String postNickname, int size, long cursorPostId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(myNickname);
		followingNickname.add(myNickname);

		if (cursorPostId == -1) {
			postList = postRepository.findUserPostsByFollow(myNickname, postNickname, followingNickname, pageRequest);
		} else {
			postList = postRepository.findUserPostsByIdAndFollow(cursorPostId, myNickname, postNickname,
				followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(myNickname);
		List<Post> likedPosts = postLikeRepository.findPostLikesByUserNickname(myNickname);

		return new PostSummaryResponse(postList, savedPosts, likedPosts);
	}

	public PostSummaryResponse getLikedUserPosts(String myNickname, String postNickname, int size, long cursorPostId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(myNickname);
		followingNickname.add(myNickname);

		if (cursorPostId == -1) {
			postList = postRepository.findLikedUserPostsByFollow(myNickname, postNickname, followingNickname,
				pageRequest);
		} else {
			postList = postRepository.findLikedUserPostsByIdAndFollow(cursorPostId, myNickname, postNickname,
				followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(myNickname);

		return PostSummaryResponse.createLikedPostSummary(postList, savedPosts);
	}

	public TempSaveResponseDto getTempPost(String nickname) {
		Optional<Post> tempPostOptional = postRepository.findPostByTempSaveIsTrueAndUserNickname(nickname);
		if (tempPostOptional.isPresent()) {
			Post tempPost = tempPostOptional.get();
			return new TempSaveResponseDto(tempPost);
		}
		return null;
	}
}
