package com.backend.naildp.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(Exception exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(RuntimeException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NullPointerException.class)
	protected ResponseEntity<ApiResponse<?>> handleNullPointerException(NullPointerException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	protected ResponseEntity<ApiResponse<?>> handleMissingRequestPartException(
		MissingServletRequestPartException ex) {
		ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
		apiResponse.setMessage("Required request part is missing");
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	// @Override
	// protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	// 	HttpHeaders headers, HttpStatusCode status, WebRequest request) {
	// 	ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
	// 	apiResponse.setMessage(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
	// 	return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	// }
	//
	// @Override
	// protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
	// 	HttpHeaders headers, HttpStatusCode status, WebRequest request) {
	// 	ApiResponse<?> apiResponse = ApiResponse.of(HttpStatus.BAD_REQUEST.value());
	// 	apiResponse.setMessage("Required request part is missing");
	// 	return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	// }

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException exception) {
		ApiResponse<?> apiResponse = ApiResponse.of(exception.getErrorCode());
		apiResponse.setMessage(exception.getMessage());
		return new ResponseEntity<>(apiResponse, HttpStatus.OK);
	}

	// @Override
	// protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
	// 	HttpHeaders headers, HttpStatusCode status, WebRequest request) {
	// 	return super.handleAsyncRequestTimeoutException(ex, headers, status, request);
	// }
}