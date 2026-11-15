package ch.usi.smb_backend.model.geo;

/**
 * A point on the earth, in WGS84. The order is longitude first, as in the database and the
 * frontend.
 */
public record GeoPoint(double longitude, double latitude, double heightM) {

	private static final double EARTH_RADIUS_M = 6_371_000;

	public GeoPoint {
		requireInRange("longitude", longitude, 180);
		requireInRange("latitude", latitude, 90);
		if (!Double.isFinite(heightM)) {
			throw new IllegalArgumentException("height must be a finite number, was " + heightM);
		}
	}

	/** Distance along the ground (great circle), ignoring the difference in height. */
	public double distanceTo(GeoPoint other) {
		double latitudeA = Math.toRadians(latitude);
		double latitudeB = Math.toRadians(other.latitude);
		double latitudeDelta = latitudeB - latitudeA;
		double longitudeDelta = Math.toRadians(other.longitude - longitude);
		double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
				+ Math.cos(latitudeA) * Math.cos(latitudeB) * Math.pow(Math.sin(longitudeDelta / 2), 2);
		return 2 * EARTH_RADIUS_M * Math.asin(Math.min(1, Math.sqrt(haversine)));
	}

	/** The point a share {@code fraction} (0..1) of the way from this point to {@code other}. */
	public GeoPoint towards(GeoPoint other, double fraction) {
		return new GeoPoint(
				interpolate(longitude, other.longitude, fraction),
				interpolate(latitude, other.latitude, fraction),
				interpolate(heightM, other.heightM, fraction));
	}

	private static double interpolate(double from, double to, double fraction) {
		return from + (to - from) * fraction;
	}

	private static void requireInRange(String name, double value, double limit) {
		if (!(value >= -limit && value <= limit)) {
			throw new IllegalArgumentException(name + " must be between -" + limit + " and " + limit + ", was " + value);
		}
	}
}
