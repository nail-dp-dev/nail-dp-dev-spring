package com.backend.naildp.dto.comment;

import java.util.ArrayList;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryResponse {

	private Long cursorId;
	private Slice<CommentInfoResponse> contents;

	public static CommentSummaryResponse createEmptyResponse() {
		return new CommentSummaryResponse(-1L, new SliceImpl<>(new ArrayList<>()));
	}
}
