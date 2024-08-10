package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.archive.ArchiveIdRequestDto;
import com.backend.naildp.dto.archive.ArchiveRequestDto;
import com.backend.naildp.dto.archive.ArchiveResponseDto;
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

	@Transactional
	public void createArchive(String nickname, ArchiveRequestDto archiveRequestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));

		if (user.getRole() == UserRole.USER) {
			int archiveCount = archiveRepository.countArchivesByUserNickname(nickname);
			if (archiveCount >= 4) {
				throw new CustomException("더이상 아카이브를 생성할 수 없습니다.", ErrorCode.INVALID_FORM);
			}
		}

		Archive archive = new Archive(user, archiveRequestDto.getArchiveName(), archiveRequestDto.getBoundary());

		archiveRepository.save(archive);

	}

	@Transactional(readOnly = true)
	public ArchiveResponseDto getArchives(String nickname) {

		List<ArchiveMapping> archives = archiveRepository.findArchiveInfosByUserNickname(nickname);

		return ArchiveResponseDto.of(archives);
	}

	@Transactional
	public void saveArchive(String nickname, Long archiveId, Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		Archive archive = archiveRepository.findArchiveById(archiveId)
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User postUser = post.getUser();
		String photo = post.getPhotos().get(0).getPhotoUrl();

		if (!archive.equalsNickname(nickname)) {
			throw new CustomException("본인의 아카이브에만 접근할 수 있습니다.", ErrorCode.USER_MISMATCH);
		}

		if (archivePostRepository.existsByArchiveIdAndPostId(archiveId, postId)) {
			throw new CustomException("이미 저장한 게시물입니다.", ErrorCode.ALREADY_EXIST);
		}

		if (post.isTempSaved()) {
			throw new CustomException("임시저장 게시물은 저장할 수 없습니다.", ErrorCode.SAVE_AUTHORITY);
		}

		if (post.isClosed()) {
			throw new CustomException("비공개 게시물은 저장할 수 없습니다.", ErrorCode.SAVE_AUTHORITY);
		}

		if (post.isOpenedForFollower() && !followRepository.existsByFollowerNicknameAndFollowing(nickname, postUser)) {
			throw new CustomException("팔로워만 게시물을 저장할 수 있습니다.", ErrorCode.SAVE_AUTHORITY);
		}

		ArchivePost archivePost = new ArchivePost(archive, post);
		archivePostRepository.save(archivePost);
		//최근게시물 사진으로 썸네일 업데이트
		archive.updateImgUrl(photo);

	}

	public void copyArchive(String nickname, ArchiveIdRequestDto requestDto) {
		User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException("해당 유저가 존재하지 않습니다.",
			ErrorCode.NOT_FOUND));

		Archive originalArchive = archiveRepository.findArchiveById(requestDto.getArchiveId())
			.orElseThrow(() -> new CustomException("해당 아카이브를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		if (!originalArchive.equalsNickname(nickname)) {
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

}
