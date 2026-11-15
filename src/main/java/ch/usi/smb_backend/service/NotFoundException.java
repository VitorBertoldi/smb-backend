package ch.usi.smb_backend.service;

/** Something asked for by id doesn't exist in the data. */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
