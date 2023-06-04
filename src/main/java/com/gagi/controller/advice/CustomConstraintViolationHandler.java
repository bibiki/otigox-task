package com.gagi.controller.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class CustomConstraintViolationHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<CustomError> handleConstraintViolationException(ConstraintViolationException exception) {
		String message = "";
		for (ConstraintViolation<?> c : exception.getConstraintViolations()) {
			message = message + c.getPropertyPath().toString() + " " + c.getMessage();
		}
		CustomError customError = new CustomError(HttpStatus.BAD_REQUEST, message);
		return ResponseEntity.badRequest().body(customError);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<CustomError> handleConstraintViolationException(DataIntegrityViolationException exception) {
		CustomError customError = new CustomError(HttpStatus.BAD_REQUEST, exception.getMessage());
		return ResponseEntity.badRequest().body(customError);
	}

	static class CustomError {
		private HttpStatus status;
		private String message;

		public CustomError(HttpStatus status, String message) {
			this.status = status;
			this.message = message;
		}

		public HttpStatus getStatus() {
			return status;
		}

		public void setStatus(HttpStatus status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}