package com.backend.naildp.repository.tag;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.naildp.entity.postEntity.Post;
import com.backend.naildp.entity.postEntity.TagPost;

public interface TagPostRepository extends JpaRepository<TagPost, Long>, TagPostSearchRepository {

	@Query("select tp from TagPost tp join fetch tp.tag t where tp.post = :post")
	List<TagPost> findTagPostAndTagByPost(@Param("post") Post post);

	@Query("select distinct tp.tag.id from TagPost tp where tp.post in :posts")
	List<Long> findTagIdsInPosts(@Param("posts") List<Post> posts);

	@Query("select distinct tp.tag.id from TagPost tp where tp.post.id in :postIds")
	List<Long> findTagIdsInPostIds(@Param("postIds") Set<Long> postIds);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from TagPost tp where tp.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);
}