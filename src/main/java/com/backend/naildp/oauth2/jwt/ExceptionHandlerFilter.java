package com.backend.naildp.oauth2.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.naildp.exception.TokenNotValidateException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (TokenNotValidateException ex) {
			setErrorResponse(HttpStatus.UNAUTHORIZED, response, ex);
		}

	}

	public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex) throws
		IOException {
		response.setStatus(status.value());
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().write(
			ErrorResponse.of(
					HttpStatus.UNAUTHORIZED.value(),
					ex.getMessage()
				)
				.convertToJson()
		);
	}
}
