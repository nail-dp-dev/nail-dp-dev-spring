package com.backend.naildp.repository;

import com.backend.naildp.common.Boundary;

public interface ArchiveMapping {
	Long getId();

	String getName();

	Boundary getBoundary();

	String getArchiveImgUrl();

	Long getPostCount();
}
