package ch.usi.smb_backend.model.timetable;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.time.TimeStatus;
import org.junit.jupiter.api.Test;

class StopEventTest {

	@Test
	void needsAtLeastOneTime() {
		assertThatIllegalArgumentException().isThrownBy(() -> StopEventBuilder.at(AARAU).buildAlone());
	}

	@Test
	void cantBeScheduledToLeaveBeforeItArrives() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> StopEventBuilder.at(AARAU).arrives("10:05").departs("10:00").buildAlone());
	}

	@Test
	void mayHaveActualTimesOutOfOrder() {
		StopEvent measured = StopEventBuilder.at(AARAU)
				.arrives("10:00", "10:03", TimeStatus.MEASURED)
				.departs("10:01", "10:02", TimeStatus.MEASURED)
				.buildAlone();

		assertThat(measured.arrivalOn(TimeBasis.ACTUAL)).isEqualTo(at("10:03"));
		assertThat(measured.departureOn(TimeBasis.ACTUAL)).isEqualTo(at("10:02"));
	}

	@Test
	void usesTheOtherTimeAtTheEndsOfATrip() {
		StopEvent origin = StopEventBuilder.at(AARAU).departs("10:00").buildAlone();
		StopEvent destination = StopEventBuilder.at(AARAU).arrives("11:00").buildAlone();

		assertThat(origin.arrivalOn(TimeBasis.SCHEDULED)).isEqualTo(at("10:00"));
		assertThat(destination.departureOn(TimeBasis.SCHEDULED)).isEqualTo(at("11:00"));
	}

	@Test
	void aPassThroughIsNeverAStop() {
		StopEvent passed = StopEventBuilder.at(AARAU).arrives("10:00").departs("10:00").passThrough().buildAlone();

		assertThat(passed.stopsOn(TimeBasis.SCHEDULED)).isFalse();
		assertThat(passed.stopsOn(TimeBasis.ACTUAL)).isFalse();
	}

	@Test
	void aCancelledStopIsInThePlanButNotInReality() {
		StopEvent cancelled = StopEventBuilder.at(AARAU).arrives("10:00").departs("10:02").cancelled().buildAlone();

		assertThat(cancelled.stopsOn(TimeBasis.SCHEDULED)).isTrue();
		assertThat(cancelled.stopsOn(TimeBasis.ACTUAL)).isFalse();
	}
}
