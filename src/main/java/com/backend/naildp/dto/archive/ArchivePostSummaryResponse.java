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
	private Slice<?> postSummaryList;

	public static ArchivePostSummaryResponse of(Slice<Post> latestPosts, List<Post> savedPosts,
		List<Post> likedPosts, String name) {
		Long cursorId = latestPosts.getContent().get(latestPosts.getNumberOfElements() - 1).getId();
		return new ArchivePostSummaryResponse(cursorId, name,
			latestPosts.map(post -> new HomePostResponse(post, savedPosts, likedPosts)));
	}

	public static ArchivePostSummaryResponse createEmptyResponse(String name) {
		return new ArchivePostSummaryResponse(-1L, name, new SliceImpl<>(new ArrayList<>()));
	}
}
