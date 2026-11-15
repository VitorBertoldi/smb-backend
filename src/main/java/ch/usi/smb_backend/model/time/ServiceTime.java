package ch.usi.smb_backend.model.time;

/**
 * A moment on a service day: seconds elapsed since the day started (local midnight). May exceed
 * 86,400, because a trip running after midnight still belongs to its service day. Turning it into
 * wall-clock text is {@link ServiceDay}'s job, since that depends on the day.
 */
public record ServiceTime(int seconds) implements Comparable<ServiceTime> {

	public static final ServiceTime START_OF_DAY = new ServiceTime(0);

	public ServiceTime {
		if (seconds < 0) {
			throw new IllegalArgumentException("service time can't be negative, was " + seconds + " s");
		}
	}

	public static ServiceTime ofSeconds(int seconds) {
		return new ServiceTime(seconds);
	}

	public ServiceTime plusSeconds(int amount) {
		return new ServiceTime(seconds + amount);
	}

	/** Seconds from this time to {@code later}; negative when {@code later} is earlier. */
	public int secondsUntil(ServiceTime later) {
		return later.seconds - seconds;
	}

	public boolean isBefore(ServiceTime other) {
		return seconds < other.seconds;
	}

	public boolean isAfter(ServiceTime other) {
		return seconds > other.seconds;
	}

	public ServiceTime max(ServiceTime other) {
		return isAfter(other) ? this : other;
	}

	@Override
	public int compareTo(ServiceTime other) {
		return Integer.compare(seconds, other.seconds);
	}

	@Override
	public String toString() {
		return seconds + " s";
	}
}
