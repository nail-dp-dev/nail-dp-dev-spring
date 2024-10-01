package com.backend.naildp.repository;

import org.springframework.data.domain.Slice;

import com.backend.naildp.entity.QArchive;
import com.backend.naildp.entity.QArchivePost;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class ArchiveRepositoryImpl implements ArchiveSearchRepository {

	private final JPAQueryFactory queryFactory;

	public ArchiveRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Slice<ArchiveMapping> searchArchiveByArchiveName(String archiveName) {
		QArchive ar = QArchive.archive;
		QArchivePost ap = QArchivePost.archivePost;

		queryFactory.select(
				ar.id
			).from(ar)
			.leftJoin(ap).on().where().orderby();
	}

}
"select a.id as id, a.name as name, a.boundary as boundary, a.archiveImgUrl as archiveImgUrl, COUNT(ap) as postCount "
	+ "from Archive a left join a.archivePosts ap "
	+ "where a.user.nickname = :nickname "
	+ "and a.id < :id "
	+ "group by a.id "
	+ "order by a.createdDate DESC")