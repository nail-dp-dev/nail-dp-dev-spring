package com.backend.naildp.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentRegisterDto {

	@NotBlank(message = "댓글을 입력해주세요")
	private String commentContent;
}
