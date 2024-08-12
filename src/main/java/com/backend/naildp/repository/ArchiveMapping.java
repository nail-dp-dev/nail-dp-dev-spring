package com.backend.naildp.repository;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.entity.User;

public interface ArchiveMapping {
	Long getId();

	String getName();

	Boundary getBoundary();

	String getArchiveImgUrl();

	Long getPostCount();

	Long getArchiveCount();

	User getNickname();

	User getThumbnailUrl();

}
