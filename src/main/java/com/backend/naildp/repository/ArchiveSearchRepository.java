package com.backend.naildp.repository;

import org.springframework.data.domain.Slice;

public interface ArchiveSearchRepository {
	Slice<ArchiveMapping> searchArchiveByArchiveName(String archiveName);

}
