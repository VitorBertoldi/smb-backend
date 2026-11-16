package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.service.UnknownServiceDateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Errors as {@code application/problem+json}. Missing or unreadable parameters (a bad date) are
 * already answered with 400 by the base class.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(UnknownServiceDateException.class)
	ProblemDetail unknownServiceDate(UnknownServiceDateException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setProperty("availableDates", exception.availableDates());
		return problem;
	}

	@ExceptionHandler(BadRequestException.class)
	ProblemDetail badRequest(BadRequestException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
