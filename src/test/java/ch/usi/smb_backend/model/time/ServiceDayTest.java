package ch.usi.smb_backend.model.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ServiceDayTest {

	private static final ServiceDay WEDNESDAY = ServiceDay.of(LocalDate.of(2026, 10, 21));
	/** The clocks went back from 03:00 to 02:00 on this day. */
	private static final ServiceDay CLOCKS_GO_BACK = ServiceDay.of(LocalDate.of(2026, 10, 25));
	/** The clocks went forward from 02:00 to 03:00 on this day. */
	private static final ServiceDay CLOCKS_GO_FORWARD = ServiceDay.of(LocalDate.of(2026, 3, 29));

	@Test
	void startsAtLocalMidnight() {
		// Summer time: midnight in Zürich is 22:00 UTC the evening before.
		assertThat(WEDNESDAY.start()).isEqualTo(Instant.parse("2026-10-20T22:00:00Z"));
	}

	@Test
	void readsWallClockText() {
		assertThat(WEDNESDAY.parseClock("09:32")).isEqualTo(ServiceTime.ofSeconds(34_320));
		assertThat(WEDNESDAY.parseClock("09:32:15")).isEqualTo(ServiceTime.ofSeconds(34_335));
		assertThat(WEDNESDAY.parseClock("25:10")).isEqualTo(ServiceTime.ofSeconds(90_600));
	}

	@ParameterizedTest
	@ValueSource(strings = {"9h32", "09:60", "09:32:60", "", "ten"})
	void rejectsTextThatIsNotAClockTime(String text) {
		assertThatIllegalArgumentException().isThrownBy(() -> WEDNESDAY.parseClock(text));
	}

	@ParameterizedTest
	@ValueSource(strings = {"00:00", "09:32", "09:32:15", "23:59", "25:10", "34:05"})
	void writesTheSameTextItReads(String clock) {
		assertThat(WEDNESDAY.formatClock(WEDNESDAY.parseClock(clock))).isEqualTo(clock);
	}

	@Test
	void convertsInstantsBothWays() {
		Instant departure = Instant.parse("2026-10-21T07:32:00Z");

		ServiceTime serviceTime = WEDNESDAY.toServiceTime(departure);

		assertThat(serviceTime).isEqualTo(ServiceTime.ofSeconds(34_320));
		assertThat(WEDNESDAY.toInstant(serviceTime)).isEqualTo(departure);
	}

	@Test
	void rejectsAnInstantBeforeTheDayStarts() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> WEDNESDAY.toServiceTime(Instant.parse("2026-10-20T21:59:59Z")));
	}

	@Test
	void countsElapsedTimeWhenTheClocksGoBack() {
		// 04:00 on the wall clock is five hours after midnight, because 02:00-03:00 happened twice.
		assertThat(CLOCKS_GO_BACK.parseClock("04:00")).isEqualTo(ServiceTime.ofSeconds(5 * 3_600));
		assertThat(CLOCKS_GO_BACK.formatClock(ServiceTime.ofSeconds(5 * 3_600))).isEqualTo("04:00");
	}

	@Test
	void countsElapsedTimeWhenTheClocksGoForward() {
		// 04:00 on the wall clock is three hours after midnight: 02:00-03:00 never happened.
		assertThat(CLOCKS_GO_FORWARD.parseClock("04:00")).isEqualTo(ServiceTime.ofSeconds(3 * 3_600));
		assertThat(CLOCKS_GO_FORWARD.formatClock(ServiceTime.ofSeconds(3 * 3_600))).isEqualTo("04:00");
	}
}
