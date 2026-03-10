package com.backend.naildp.service.dto;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.userEntity.User;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostLikeEventDto {

    private Long postId;
    private UUID writerId;

    public PostLikeEventDto(Long postId, UUID writerId) {
        this.postId = postId;
        this.writerId = writerId;
    }

    public static PostLikeEventDto of(Post post, User user) {
        return new PostLikeEventDto(post.getId(), user.getId());
    }
}
