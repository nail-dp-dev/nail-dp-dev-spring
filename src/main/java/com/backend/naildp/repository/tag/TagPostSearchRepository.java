package com.backend.naildp.repository.tag;

import java.util.List;

import com.backend.naildp.entity.postEntity.TagPost;

public interface TagPostSearchRepository {

	List<TagPost> searchRelatedTags(List<String> keywords, String userNickname);
}
