package com.gagi.controller.advice;

import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class CustomConstraintViolationHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public CustomError handleException(ConstraintViolationException exception) {
		String message = "";
		for (ConstraintViolation<?> c : exception.getConstraintViolations()) {
			message = message + c.getPropertyPath().toString() + " " + c.getMessage();
		}
		return new CustomError(message);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public CustomError handleException(DataIntegrityViolationException exception) {
		return new CustomError(exception.getMessage());
	}
	
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	@ResponseBody
	public CustomError handleException(NoSuchElementException exception) {
		return new CustomError(exception.getMessage());
	}

	static class CustomError {
		private String message;

		public CustomError(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}