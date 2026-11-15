package ch.usi.smb_backend.model.network;

/**
 * Where a vehicle stops within a station, identified by its stop-edge sloid
 * (e.g. {@code ch:1:sloid:3000:503:34}). Istdaten has no platform name.
 */
public record Platform(String sloid) {

	private static final String SEPARATOR = ":";

	public Platform {
		if (sloid == null || sloid.isBlank()) {
			throw new IllegalArgumentException("a platform sloid can't be blank");
		}
	}

	/** The last part of the sloid ({@code 34}), until the data gives a real platform name. */
	public String label() {
		return sloid.substring(sloid.lastIndexOf(SEPARATOR) + 1);
	}
}
