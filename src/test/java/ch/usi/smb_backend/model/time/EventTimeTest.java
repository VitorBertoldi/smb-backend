package ch.usi.smb_backend.model.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class EventTimeTest {

	private static final ServiceTime PLANNED = ServiceTime.ofSeconds(34_320);
	private static final ServiceTime HAPPENED = ServiceTime.ofSeconds(34_390);

	@Test
	void anActualTimeComesExactlyWithAKnownStatus() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new EventTime(PLANNED, Optional.empty(), TimeStatus.MEASURED));
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new EventTime(PLANNED, Optional.of(HAPPENED), TimeStatus.UNKNOWN));
	}

	@Test
	void answersTheTimeOnEachBasis() {
		EventTime measured = EventTime.of(PLANNED, HAPPENED, TimeStatus.MEASURED);

		assertThat(measured.on(TimeBasis.SCHEDULED)).isEqualTo(PLANNED);
		assertThat(measured.on(TimeBasis.ACTUAL)).isEqualTo(HAPPENED);
	}

	@Test
	void fallsBackToThePlanWhenNothingWasMeasured() {
		EventTime unknown = EventTime.scheduledOnly(PLANNED);

		assertThat(unknown.on(TimeBasis.ACTUAL)).isEqualTo(PLANNED);
		assertThat(unknown.delay()).isEmpty();
		assertThat(unknown.isReliableOn(TimeBasis.ACTUAL)).isFalse();
	}

	@Test
	void measuresTheDelay() {
		assertThat(EventTime.of(PLANNED, HAPPENED, TimeStatus.MEASURED).delay()).contains(Delay.ofSeconds(70));
	}

	@Test
	void trustsThePlanAndReliableMeasurementsOnly() {
		EventTime forecast = EventTime.of(PLANNED, HAPPENED, TimeStatus.FORECAST);
		EventTime estimated = EventTime.of(PLANNED, HAPPENED, TimeStatus.ESTIMATED);

		assertThat(forecast.isReliableOn(TimeBasis.SCHEDULED)).isTrue();
		assertThat(forecast.isReliableOn(TimeBasis.ACTUAL)).isFalse();
		assertThat(estimated.isReliableOn(TimeBasis.ACTUAL)).isTrue();
	}
}
