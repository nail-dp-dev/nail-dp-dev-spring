package com.backend.naildp.service.post;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.common.Boundary;
import com.backend.naildp.common.UserRole;
import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.entity.Archive;
import com.backend.naildp.entity.ArchivePost;
import com.backend.naildp.entity.Photo;
import com.backend.naildp.entity.Post;
import com.backend.naildp.entity.PostLike;
import com.backend.naildp.entity.Tag;
import com.backend.naildp.entity.TagPost;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.PostRepository;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles(profiles = {"test", "secret"})
@SpringBootTest
@Transactional
class ForyouPostStrategyTest {

	@Autowired
	ForyouPostStrategy foryouPostStrategy;

	@Autowired
	EntityManager em;

	@BeforeEach
	void setup() {
		Tag likedTag = createTag("좋아요태그");
		Tag savedTag = createTag("저장태그");
		Tag tag = createTag("그냥태그");

		User user = createUserWithNickname("jjw");
		Archive archive = createArchiveOfUser(user);

		User postWriter = createUserWithNickname("writer");

		Post postContainingLikedTag = createPostOfUser(postWriter, false, Boundary.ALL);
		addTagToPost(List.of(likedTag), postContainingLikedTag);
		Post postContainingSavedTag = createPostOfUser(postWriter, false, Boundary.ALL);
		addTagToPost(List.of(savedTag), postContainingSavedTag);
		Post post = createPostOfUser(postWriter, false, Boundary.ALL);
		addTagToPost(List.of(tag), post);
		Post postContainingSelectedTags = createPostOfUser(postWriter, false, Boundary.ALL);
		addTagToPost(List.of(likedTag, savedTag), postContainingSelectedTags);

		em.persist(new PostLike(user, postContainingLikedTag));
		em.persist(new ArchivePost(archive, postContainingSavedTag));

		em.flush();
		em.clear();
	}

	@DisplayName("for you 게시물 조회시 사용자가 저장하거나 좋아요한 게시물의 태그를 포함한다.")
	@Test
	void forYouPostsContainsTagsOfSelectedPosts() {
		//given
		String username = "jjw";
		int size = 10;

		//when
		PostSummaryResponse response = foryouPostStrategy.homePosts(size, null, username);

		//then
		List<HomePostResponse> homePostResponses = (List<HomePostResponse>)response.getPostSummaryList().getContent();
		List<Long> findPostIds = homePostResponses.stream().map(HomePostResponse::getPostId).collect(Collectors.toList());
		List<Post> posts = findPostsById(findPostIds);
		List<String> findTagNames = posts.stream()
			.flatMap(post -> post.getTagPosts().stream())
			.map(tagPost -> tagPost.getTag().getName())
			.distinct()
			.toList();

		assertThat(homePostResponses).hasSize(3);
		assertThat(findTagNames).containsExactlyInAnyOrder("좋아요태그", "저장태그");
		assertThat(findTagNames).doesNotContain("그냥 태그");
	}

	private List<Post> findPostsById(List<Long> postIds) {
		return em.createQuery("select p from Post p where p.id in :ids", Post.class)
			.setParameter("ids", postIds)
			.getResultList();
	}

	private User createUserWithNickname(String nickname) {
		User user = User.builder()
			.nickname(nickname)
			.phoneNumber("")
			.agreement(true)
			.thumbnailUrl("")
			.role(UserRole.USER)
			.build();
		em.persist(user);
		return user;
	}

	private Archive createArchiveOfUser(User user) {
		Archive archive = Archive.builder()
			.user(user)
			.name("")
			.archiveImgUrl("")
			.boundary(Boundary.ALL)
			.build();
		em.persist(archive);
		return archive;
	}

	private Post createPostOfUser(User user, boolean tempSave, Boundary boundary) {
		Post post = Post.builder()
			.user(user)
			.postContent("")
			.tempSave(tempSave)
			.boundary(boundary)
			.build();
		em.persist(post);
		createPhotoInPost(post);
		return post;
	}

	private Photo createPhotoInPost(Post post) {
		Photo photo = new Photo(post, new FileRequestDto("", 1L, ""));
		post.addPhoto(photo);
		em.persist(photo);
		return photo;
	}

	private void addTagToPost(List<Tag> tags, Post post) {
		tags.stream().map(tag -> new TagPost(tag, post))
			.forEach(tagPost -> {
				post.addTagPost(tagPost);
				em.persist(tagPost);
			});
	}

	private Tag createTag(String tagName) {
		Tag tag = new Tag(tagName);
		em.persist(tag);
		return tag;
	}
}