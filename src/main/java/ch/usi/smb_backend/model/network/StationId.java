package ch.usi.smb_backend.model.network;

/** A station's 7-digit code (e.g. {@code 8503000}): the key that joins every source. */
public record StationId(String code) {

	public StationId {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("a station id can't be blank");
		}
	}

	@Override
	public String toString() {
		return code;
	}
}
