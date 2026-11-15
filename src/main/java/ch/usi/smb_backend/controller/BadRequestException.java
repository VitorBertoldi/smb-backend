package ch.usi.smb_backend.controller;

/** A request parameter is present but can't be used, e.g. a time like {@code 9h32}. */
class BadRequestException extends RuntimeException {

	BadRequestException(String message) {
		super(message);
	}
}
