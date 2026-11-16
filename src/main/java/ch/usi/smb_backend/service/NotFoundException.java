package ch.usi.smb_backend.service;

/** Something asked for by id that the database does not have. */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
