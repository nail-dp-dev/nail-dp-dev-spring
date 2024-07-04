package com.backend.naildp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.dto.home.HomePostResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.PostService;

@WebMvcTest(HomeController.class)
class HomeControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	PostService postService;

	@Test
	@WithMockUser(username = "testUser", roles = {"USER"})
	void test() throws Exception {
		List<HomePostResponse> homePostResponses = new ArrayList<>();
		ApiResponse<List<HomePostResponse>> apiResponse = ApiResponse.successResponse(homePostResponses, "최신 게시물 조회",
			2000);

		when(postService.homePosts(anyString())).thenReturn(homePostResponses);

		mvc.perform(get("/home").param("choice", "NEW"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(apiResponse.getMessage()))
			.andExpect(jsonPath("$.code").value(apiResponse.getCode()));
	}

}