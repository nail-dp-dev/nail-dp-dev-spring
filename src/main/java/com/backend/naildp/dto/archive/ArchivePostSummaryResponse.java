package com.backend.naildp.dto.archive;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchivePostSummaryResponse {
	private Long cursorId;
	private String archiveName;
	private String nickname;
	private Slice<?> postSummaryList;

	public static ArchivePostSummaryResponse of(Slice<Post> latestPosts, List<Post> savedPosts,
		List<Post> likedPosts, String archiveName, String nickname) {
		Long cursorId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		return new ArchivePostSummaryResponse(cursorId, archiveName, nickname,
			latestPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts)));
	}

	public static ArchivePostSummaryResponse createEmptyResponse(String archiveName, String nickname) {
		return new ArchivePostSummaryResponse(-1L, archiveName, nickname, new SliceImpl<>(new ArrayList<>()));
	}
}
