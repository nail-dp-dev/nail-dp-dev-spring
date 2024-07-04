package com.backend.naildp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.naildp.service.PostService;

@WebMvcTest(HomeController.class)
class HomeControllerUnitTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	PostService postService;

	@Test
	void test() {
		
	}

}