package com.backend.naildp.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.ArchivePostSummaryResponse;
import com.backend.naildp.dto.archive.CreateArchiveRequestDto;
import com.backend.naildp.dto.archive.UnsaveRequestDto;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.User;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;
import com.backend.naildp.repository.ArchiveMapping;
import com.backend.naildp.repository.ArchivePostRepository;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.FollowRepository;
import com.backend.naildp.repository.PostLikeRepository;
import com.backend.naildp.repository.PostMapping;
import com.backend.naildp.repository.PostRepository;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {
	private final UserRepository userRepository;
	private final ArchiveRepository archiveRepository;
	private final ArchivePostRepository archivePostRepository;
	private final PostRepository postRepository;
	private final FollowRepository followRepository;
	private final PostLikeRepository postLikeRepository;

	@Transactional
	public Long createArchive(String nickname, CreateArchiveRequestDto createArchiveRequestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));

		if (user.getRole() == UserRole.USER) {
			int archiveCount = archiveRepository.countArchivesByUserNickname(nickname);
			if (archiveCount >= 4) {
				throw new CustomException("더이상 아카이브를 생성할 수 없습니다.", ErrorCode.INVALID_FORM);
			}
		}

		Archive archive = new Archive(user, createArchiveRequestDto.getArchiveName(),
			createArchiveRequestDto.getBoundary());

		archiveRepository.save(archive);
		return archive.getId();

	}

	@Transactional(readOnly = true)
	public PostSummaryResponse getMyArchives(String nickname, int size, Long cursorId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<ArchiveMapping> archiveList;

		if (cursorId == -1) {
			archiveList = archiveRepository.findArchiveInfosByUserNickname(nickname, pageRequest);
		} else {
			archiveList = archiveRepository.findArchiveInfosByIdAndUserNickname(nickname, cursorId, pageRequest);

		}
		if (archiveList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return PostSummaryResponse.createUserArchiveSummary(archiveList);
	}

	@Transactional(readOnly = true)
	public PostSummaryResponse getOtherArchives(String myNickname, String otherNickname, int size, Long cursorId) {

		User otherUser = userRepository.findByNickname(otherNickname)
			.orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
				ErrorCode.NOT_FOUND));

		//사용자가 아카이브 생성자의 팔로워인지 확인필요. -> isLock(잠금 썸네일)
		boolean isFollower = followRepository.existsByFollowerNicknameAndFollowing(myNickname, otherUser);

		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<ArchiveMapping> archiveList;

		if (cursorId == -1) {
			archiveList = archiveRepository.findArchiveInfosWithoutNone(otherNickname, pageRequest);
		} else {
			archiveList = archiveRepository.findArchiveInfosByIdWithoutNone(otherNickname, cursorId, pageRequest);

		}
		if (archiveList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		return PostSummaryResponse.createOtherArchiveSummary(archiveList, isFollower);
	}

	@Transactional
	public void saveArchive(String nickname, Long archiveId, Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User postUser = post.getUser();
		String photo = post.getPhotos().get(0).getPhotoUrl();

		if (archive.notEqualsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}

		if (archivePostRepository.existsByArchiveIdAndPostId(archiveId, postId)) {
			throw new CustomException("이미 저장한 게시물입니다.", ErrorCode.ALREADY_EXIST);
		}

		if (post.isTempSaved()) {
			throw new CustomException("임시저장 게시물은 저장할 수 없습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isClosed() && post.notWrittenBy(nickname)) {
			throw new CustomException("비공개 게시물은 저장할 수 없습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(nickname, postUser)
			&& post.notWrittenBy(nickname)) {
			throw new CustomException("팔로워만 게시물을 저장할 수 있습니다.", ErrorCode.INVALID_BOUNDARY);
		}

		ArchivePost archivePost = new ArchivePost(archive, post);
		archivePostRepository.save(archivePost);
		//최근게시물 사진으로 썸네일 업데이트
		archive.updateImgUrl(photo);

	}

	@Transactional
	public void copyArchive(String nickname, ArchiveIdRequestDto requestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));

		Archive originalArchive = archiveRepository.findArchiveById(requestDto.getArchiveId())
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (originalArchive.notEqualsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}

		List<PostMapping> postList = archivePostRepository.findArchivePostsByArchiveId(requestDto.getArchiveId());

		Archive copyArchive = Archive.builder()
			.archiveImgUrl(originalArchive.getArchiveImgUrl())
			.name(originalArchive.getName() + "Copy")
			.boundary(originalArchive.getBoundary())
			.user(user)
			.build();

		archiveRepository.save(copyArchive);

		if (!postList.isEmpty()) {
			postList.stream()
				.map(post -> new ArchivePost(copyArchive, post.getPost()))
				.forEach(archivePostRepository::save);
		}
	}

	@Transactional(readOnly = true)
	public PostSummaryResponse getFollowingArchives(String nickname, int size, Long cursorId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<ArchiveMapping> archiveList;
		// 팔로잉 nickname 썸네일사진, 아카이브 썸네일, archive count 아카이브 ID
		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(nickname);

		// followingNickname.add(nickname);
		if (cursorId == -1) {
			archiveList = archiveRepository.findArchivesByFollowing(followingNickname, pageRequest);
		} else {
			archiveList = archiveRepository.findArchivesByIdAndFollowing(followingNickname, cursorId, pageRequest);

		}
		if (archiveList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}
		return PostSummaryResponse.createFollowArchiveSummary(archiveList);
	}

	@Transactional
	public void deleteArchive(String nickname, Long archiveId) {

		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (archive.notEqualsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}
		archivePostRepository.deleteAllByArchiveId(archive.getId());
		archiveRepository.delete(archive);
	}

	@Transactional(readOnly = true)
	public ArchivePostSummaryResponse getArchivePosts(String nickname, Long archiveId, int size,
		long cursorId) {
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (archive.isClosed() && archive.notEqualsNickname(nickname)) {
			throw new CustomException("비공개 아카이브입니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (archive.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(nickname,
			archive.getUser()) && archive.notEqualsNickname(nickname)) {
			throw new CustomException("팔로워 아카이브입니다.", ErrorCode.INVALID_BOUNDARY);
		}

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(nickname);
		followingNickname.add(nickname);

		if (cursorId == -1) {
			postList = postRepository.findArchivePostsByFollow(nickname, archiveId, followingNickname, pageRequest);
		} else {
			postList = postRepository.findArchivePostsByIdAndFollow(cursorId, nickname, archiveId,
				followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return ArchivePostSummaryResponse.createEmptyResponse(archive.getName());
		}

		List<Post> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(nickname);
		List<Post> likedPosts = postLikeRepository.findPostLikesByUserNickname(nickname);

		return ArchivePostSummaryResponse.of(postList, savedPosts, likedPosts, archive.getName());
	}

	@Transactional(readOnly = true)
	public PostSummaryResponse getLikedArchivePosts(String nickname, Long archiveId, int size, long cursorId) {

		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Post> postList;

		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (archive.isClosed() && archive.notEqualsNickname(nickname)) {
			throw new CustomException("비공개 아카이브입니다.", ErrorCode.INVALID_BOUNDARY);
		}

		if (archive.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(nickname,
			archive.getUser()) && archive.notEqualsNickname(nickname)) {
			throw new CustomException("팔로워 아카이브입니다.", ErrorCode.INVALID_BOUNDARY);
		}

		List<String> followingNickname = followRepository.findFollowingNicknamesByUserNickname(nickname);
		followingNickname.add(nickname);

		if (cursorId == -1) {
			postList = postRepository.findLikedArchivePostsByFollow(nickname, archiveId, followingNickname,
				pageRequest);
		} else {
			postList = postRepository.findLikedArchivePostsByIdAndFollow(cursorId, nickname, archiveId,
				followingNickname,
				pageRequest);
		}

		if (postList.isEmpty()) {
			return PostSummaryResponse.createEmptyResponse();
		}

		List<Post> savedPosts = archivePostRepository.findArchivePostsByArchiveUserNickname(nickname);

		return PostSummaryResponse.createLikedPostSummary(postList, savedPosts);
	}

	@Transactional
	public void changeArchiveName(String nickname, Long archiveId, String archiveName) {
		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (archive.notEqualsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}
		archive.updateName(archiveName);
	}

	@Transactional
	public void changeArchiveBoundary(String nickname, Long archiveId, Boundary boundary) {
		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (archive.notEqualsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}
		archive.updateBoundary(boundary);
	}

	@Transactional
	public void unsaveFromArchive(String nickname, UnsaveRequestDto unsaveRequestDto) {
		unsaveRequestDto.getArchiveId().forEach(
			archiveId -> {
				Archive archive = archiveRepository.findArchiveById(archiveId)
					.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

				if (archive.notEqualsNickname(nickname)) {
					throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
				}
			});

		archivePostRepository.deleteAllByPostIdAndArchiveId(unsaveRequestDto.getPostId(),
			unsaveRequestDto.getArchiveId());

	}
}
