package ch.usi.smb_backend.model.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One operating day, and the only place where instants and wall-clock text are converted to and
 * from {@link ServiceTime}. Service time counts elapsed seconds, so it never repeats or jumps on a
 * daylight-saving day; only the wall-clock text does.
 */
public record ServiceDay(LocalDate date, ZoneId zone) {

	public static final ZoneId SWITZERLAND = ZoneId.of("Europe/Zurich");

	private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;

	public ServiceDay {
		Objects.requireNonNull(date, "date");
		Objects.requireNonNull(zone, "zone");
	}

	/** A Swiss service day. */
	public static ServiceDay of(LocalDate date) {
		return new ServiceDay(date, SWITZERLAND);
	}

	/** Local midnight at the start of the day. */
	public Instant start() {
		return date.atStartOfDay(zone).toInstant();
	}

	public ServiceTime toServiceTime(Instant instant) {
		long seconds = Duration.between(start(), instant).toSeconds();
		if (seconds < 0) {
			throw new IllegalArgumentException(instant + " is before the start of service day " + date);
		}
		return ServiceTime.ofSeconds(Math.toIntExact(seconds));
	}

	public Instant toInstant(ServiceTime time) {
		return start().plusSeconds(time.seconds());
	}

	/**
	 * Reads wall-clock text on this day: {@code "09:32"}, {@code "09:32:15"}, or {@code "25:10"}
	 * for 01:10 the next morning.
	 *
	 * @throws IllegalArgumentException when the text is not a clock time
	 */
	public ServiceTime parseClock(String text) {
		Matcher clock = CLOCK.matcher(text.strip());
		if (!clock.matches()) {
			throw new IllegalArgumentException("'" + text + "' is not a time like 09:32 or 25:10");
		}
		int hours = Integer.parseInt(clock.group(1));
		int minutes = Integer.parseInt(clock.group(2));
		int seconds = clock.group(3) == null ? 0 : Integer.parseInt(clock.group(3));
		if (minutes >= MINUTES_PER_HOUR || seconds >= SECONDS_PER_MINUTE) {
			throw new IllegalArgumentException("'" + text + "' has minutes or seconds above 59");
		}
		LocalDateTime wallClock = date.atStartOfDay().plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
		return toServiceTime(ZonedDateTime.of(wallClock, zone).toInstant());
	}

	/** Wall-clock text: {@code "09:32"}, {@code "09:32:15"}, {@code "25:10"}; never wraps at midnight. */
	public String formatClock(ServiceTime time) {
		ZonedDateTime local = toInstant(time).atZone(zone);
		long daysLater = ChronoUnit.DAYS.between(date, local.toLocalDate());
		long hours = local.getHour() + daysLater * HOURS_PER_DAY;
		String clock = "%02d:%02d".formatted(hours, local.getMinute());
		return local.getSecond() == 0 ? clock : clock + ":%02d".formatted(local.getSecond());
	}
}
