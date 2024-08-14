package com.backend.naildp.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.dto.archive.FollowArchiveResponseDto;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.repository.ArchiveMapping;
import com.backend.naildp.repository.ArchiveRepository;
import com.backend.naildp.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
public class GetArchiveServiceTest {

	@Mock
	private FollowRepository followRepository;

	@Mock
	private ArchiveRepository archiveRepository;

	@InjectMocks
	private ArchiveService archiveService;

	@Test
	void getFollowArchives() {
		// Given
		String nickname = "testUser";
		Long cursorId = 1L;
		int size = 5;

		List<String> followingNickname = Arrays.asList("user1", "user2", nickname);
		Slice<ArchiveMapping> archiveSlice = new SliceImpl<>(Arrays.asList(
			new ArchiveMappingImpl(1L, "Archive 1", Boundary.ALL, "imgUrl1", 3L, 10L, "user1", "thumbUrl1"),
			new ArchiveMappingImpl(2L, "Archive 2", Boundary.ALL, "imgUrl2", 2L, 10L, "user2", "thumbUrl2")
		));

		given(followRepository.findFollowingNicknamesByUserNickname(nickname)).willReturn(followingNickname);
		given(archiveRepository.findArchivesByIdAndFollowing(followingNickname, cursorId, PageRequest.of(0, size)))
			.willReturn(archiveSlice);

		// When
		PostSummaryResponse response = archiveService.getFollowingArchives(nickname, size, cursorId);

		// Then
		then(response).isNotNull();
		then(response.getCursorId()).isEqualTo(2L);
		then(response.getPostSummaryList()).hasSize(2);

		FollowArchiveResponseDto archive1 = (FollowArchiveResponseDto)response.getPostSummaryList().getContent().get(0);
		FollowArchiveResponseDto archive2 = (FollowArchiveResponseDto)response.getPostSummaryList().getContent().get(1);

		then(archive1.getArchiveId()).isEqualTo(1L);
		then(archive1.getNickname()).isEqualTo("user1");
		then(archive1.getArchiveImgUrl()).isEqualTo("imgUrl1");

		then(archive2.getArchiveId()).isEqualTo(2L);
		then(archive2.getNickname()).isEqualTo("user2");
		then(archive2.getArchiveImgUrl()).isEqualTo("imgUrl2");

	}

	@Test
	void getFollowArchives_empty() {
		// Given
		String nickname = "testUser";
		Long cursorId = -1L;
		int size = 5;

		List<String> followingNickname = Arrays.asList("user1", "user2", nickname);
		Slice<ArchiveMapping> emptyArchiveSlice = new SliceImpl<>(Collections.emptyList());

		given(followRepository.findFollowingNicknamesByUserNickname(nickname)).willReturn(followingNickname);
		given(archiveRepository.findArchivesByFollowing(followingNickname, PageRequest.of(0, size)))
			.willReturn(emptyArchiveSlice);

		// When
		PostSummaryResponse response = archiveService.getFollowingArchives(nickname, size, cursorId);

		// Then
		then(response).isNotNull();
		then(response.getCursorId()).isEqualTo(-1L);
		then(response.getPostSummaryList()).isEmpty();
	}

	private static class ArchiveMappingImpl implements ArchiveMapping {
		private final Long id;
		private final String name;
		private final Boundary boundary;
		private final String archiveImgUrl;
		private final Long postCount;
		private final Long archiveCount;
		private final String nickname;
		private final String thumbnailUrl;

		public ArchiveMappingImpl(Long id, String name, Boundary boundary, String archiveImgUrl,
			Long postCount, Long archiveCount, String nickname, String thumbnailUrl) {
			this.id = id;
			this.name = name;
			this.boundary = boundary;
			this.archiveImgUrl = archiveImgUrl;
			this.postCount = postCount;
			this.archiveCount = archiveCount;
			this.nickname = nickname;
			this.thumbnailUrl = thumbnailUrl;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Boundary getBoundary() {
			return boundary;
		}

		@Override
		public String getArchiveImgUrl() {
			return archiveImgUrl;
		}

		@Override
		public Long getPostCount() {
			return postCount;
		}

		@Override
		public Long getArchiveCount() {
			return archiveCount;
		}

		@Override
		public String getNickname() {
			return nickname;
		}

		@Override
		public String getThumbnailUrl() {
			return thumbnailUrl;
		}
	}

}
