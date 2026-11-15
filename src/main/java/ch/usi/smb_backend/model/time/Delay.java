package ch.usi.smb_backend.model.time;

/** How late something happened: actual minus scheduled, in seconds; negative when early. */
public record Delay(int seconds) {

	/** Swiss operators count a vehicle as punctual when it is less than 3 minutes late. */
	public static final int ON_TIME_LIMIT_SECONDS = 3 * 60;

	private static final int SECONDS_PER_MINUTE = 60;

	public static Delay ofSeconds(int seconds) {
		return new Delay(seconds);
	}

	public static Delay between(ServiceTime scheduled, ServiceTime actual) {
		return new Delay(scheduled.secondsUntil(actual));
	}

	public boolean isOnTime() {
		return seconds < ON_TIME_LIMIT_SECONDS;
	}

	/** {@code "+1:10"} for 70 s late, {@code "-0:30"} for 30 s early. */
	@Override
	public String toString() {
		int magnitude = Math.abs(seconds);
		String sign = seconds < 0 ? "-" : "+";
		return "%s%d:%02d".formatted(sign, magnitude / SECONDS_PER_MINUTE, magnitude % SECONDS_PER_MINUTE);
	}
}
