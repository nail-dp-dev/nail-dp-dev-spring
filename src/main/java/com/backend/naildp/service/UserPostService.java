package com.backend.naildp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.userInfo.TempSaveResponseDto;
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
		followingNickname.add(myNickname);

		if (cursorPostId == -1) {
			postList = postRepository.findUserPostsByFollow(myNickname, followingNickname, pageRequest);
		} else {
			postList = postRepository.findUserPostsByIdAndFollow(cursorPostId, myNickname, followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<PostMapping> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(myNickname);
		List<PostMapping> likedPosts = postLikeRepository.findPostLikesByUserNickname(myNickname);

		return new PostSummaryResponse(postList, savedPosts, likedPosts);
	}

	public PostSummaryResponse getLikedUserPosts(String myNickname, String postNickname, int size, long cursorPostId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(myNickname);
		followingNickname.add(myNickname);

		if (cursorPostId == -1) {
			postList = postRepository.findLikedUserPostsByFollow(myNickname, followingNickname, pageRequest);
		} else {
			postList = postRepository.findLikedUserPostsByIdAndFollow(cursorPostId, myNickname, followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<PostMapping> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(myNickname);

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