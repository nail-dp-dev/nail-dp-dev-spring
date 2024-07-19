package com.backend.naildp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.dto.home.PostSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	PostService postService;

	@Autowired
	ObjectMapper objectMapper;

	@DisplayName("최신 게시물 조회 API 테스트 - 첫 호출")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void newPostsApiTest() throws Exception {
		//given
		PostSummaryResponse postSummaryResponse = new PostSummaryResponse(100L, createSlicePostResponses(true));
		ApiResponse<?> apiResponse = ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회",
			2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(postService.homePosts(eq("NEW"), anyInt(), eq(-1L), eq("testUser"))).thenReturn(postSummaryResponse);

		//when & then
		mvc.perform(get("/home").param("choice", "NEW"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andExpect(jsonPath("$.data.postSummaryList.last").value(false))
			.andDo(print());
	}

	@DisplayName("최신 게시물 조회 API 테스트 - 두번째 호출")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void newPostsApiTestSecondCall() throws Exception {
		//given
		PostSummaryResponse postSummaryResponse = new PostSummaryResponse(100L, createSlicePostResponses(false));
		ApiResponse<?> apiResponse = ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회",
			2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
		paramMap.add("choice", "NEW");
		paramMap.add("size", "20");
		paramMap.add("cursorPostId", "10");
		when(postService.homePosts(eq("NEW"), anyInt(), anyLong(), eq("testUser"))).thenReturn(postSummaryResponse);

		mvc.perform((get("/home").queryParams(paramMap)))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andExpect(jsonPath("$.data.postSummaryList.last").value(true))
			.andDo(print());
	}

	@DisplayName("로그인 안한 사용자 최신 게시물 조회 API 테스트 - 첫번째 호출")
	@Test
	void newPostsApiTestWithLogoutUserFirstCall() throws Exception {
		//given
		PostSummaryResponse postSummaryResponse = new PostSummaryResponse(100L, createSlicePostResponses(true));
		ApiResponse<?> apiResponse = ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회",
			2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(postService.homePosts(eq("NEW"), anyInt(), anyLong(), eq(""))).thenReturn(postSummaryResponse);

		// when & then
		mvc.perform((get("/home").param("choice", "NEW")))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andExpect(jsonPath("$.data.postSummaryList.last").value(false))
			.andDo(print());
	}

	@DisplayName("로그인 안한 사용자 최신 게시물 조회 API 테스트 - 두번째 호출")
	@Test
	void newPostsApiTestWithLogoutUserSecondCall() throws Exception {
		//given
		PostSummaryResponse postSummaryResponse = new PostSummaryResponse(100L, createSlicePostResponses(false));
		ApiResponse<?> apiResponse = ApiResponse.successResponse(postSummaryResponse, "최신 게시물 조회",
			2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(postService.homePosts(eq("NEW"), anyInt(), anyLong(), eq(""))).thenReturn(postSummaryResponse);

		// when & then
		mvc.perform((get("/home").param("choice", "NEW")))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andExpect(jsonPath("$.data.postSummaryList.last").value(true))
			.andDo(print());
	}

	@DisplayName("좋아요한 게시물 조회 API 테스트")
	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void likedPostApiTest() throws Exception {
		//given
		List<HomePostResponse> likedPostResponses = createLikedPostResponses();
		PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
		Page<HomePostResponse> pagedLikedPostResponses = new PageImpl<>(likedPostResponses, pageRequest, 20);
		ApiResponse<Page<HomePostResponse>> apiResponse = ApiResponse.successResponse(pagedLikedPostResponses,
			"좋아요 체크한 게시물 조회", 2000);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		when(postService.findLikedPost(anyString(), eq(0))).thenReturn(pagedLikedPostResponses);

		//when & then
		mvc.perform(get("/posts/like"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().json(jsonResponse))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()))
			.andExpect(jsonPath("$.data.content").isArray())
			.andDo(print());

	}

	private List<HomePostResponse> createLikedPostResponses() {
		List<HomePostResponse> likedPostResponses = new ArrayList<>();
		for (long i = 1; i <= 20; i++) {
			HomePostResponse likedPostResponse = createHomePostResponse(i);
			likedPostResponses.add(likedPostResponse);
		}
		return likedPostResponses;
	}

	private Slice<HomePostResponse> createSlicePostResponses(boolean hasNext) {
		List<HomePostResponse> postResponses = new ArrayList<>();
		for (long i = 1; i <= 20; i++) {
			HomePostResponse likedPostResponse = createHomePostResponse(i);
			postResponses.add(likedPostResponse);
		}
		// List<HomePostResponse> newPostResponses = createLikedPostResponses();
		PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
		return new SliceImpl<>(postResponses, pageRequest, hasNext);
	}

	private HomePostResponse createHomePostResponse(long i) {
		return HomePostResponse.builder()
			.postId(i)
			.photoId(i)
			.photoUrl("" + i)
			.like(true)
			.saved(true)
			.build();
	}
}