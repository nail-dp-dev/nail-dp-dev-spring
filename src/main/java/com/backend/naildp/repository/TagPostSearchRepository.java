package com.backend.naildp.repository;

import java.util.List;

import com.backend.naildp.entity.TagPost;

public interface TagPostSearchRepository {

	List<TagPost> searchRelatedTags(String keyword, String userNickname);
}
