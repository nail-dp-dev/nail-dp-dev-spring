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
	public void saveArchive(String nickname, Long postId, ArchiveIdRequestDto requestDto) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

		User postUser = post.getUser();
		String photo = post.getPhotos().get(0).getPhotoUrl();

		if (archivePostRepository.existsByArchiveIdAndPostId(requestDto.getArchiveId(), postId)) {
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

		Archive archive = archiveRepository.findArchiveById(requestDto.getArchiveId())
			.orElseThrow(() -> new CustomException("해당 아카이브가 존재하지 않습니다.", ErrorCode.NOT_FOUND));

		ArchivePost archivePost = new ArchivePost(archive, post);
		archivePostRepository.save(archivePost);

		archive.updateImgUrl(photo);

	}
}
