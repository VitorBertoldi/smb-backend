package ch.usi.smb_backend.model.timetable;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU_TO_BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN_TO_AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_TO_AARAU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestNetwork;
import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.fixtures.TripBuilder;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.TimeBasis;
import org.junit.jupiter.api.Test;

class TripTest {

	@Test
	void needsAtLeastTwoStopEvents() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> TripBuilder.trip("t").stops(StopEventBuilder.at(BERN).departs("10:00")).build())
				.withMessageContaining("at least 2");
	}

	@Test
	void scheduledTimeCantGoBackwards() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> TripBuilder.trip("t")
						.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("09:59"))
						.build())
				.withMessageContaining("goes backwards");
	}

	@Test
	void aPathSegmentMustLeadToTheNextStation() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> TripBuilder.trip("t")
						.stops(StopEventBuilder.at(AARAU).departs("10:00").then(BERN_TO_AARAU),
								StopEventBuilder.at(BERN).arrives("10:30"))
						.build())
				.withMessageContaining("doesn't lead");
	}

	@Test
	void theLastStopHasNoPathSegment() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> TripBuilder.trip("t")
						.stops(StopEventBuilder.at(ZURICH_HB).departs("10:00").then(ZURICH_TO_AARAU),
								StopEventBuilder.at(AARAU).arrives("10:30").then(AARAU_TO_BERN))
						.build())
				.withMessageContaining("after its last stop");
	}

	@Test
	void namesItsDestinationAndItself() {
		Trip ic712 = TestTrips.ic712();

		assertThat(ic712.origin()).isEqualTo(ZURICH_HB);
		assertThat(ic712.destination()).isEqualTo(BERN);
		assertThat(ic712.displayName()).isEqualTo("IC 712");
	}

	@Test
	void usesTheLineNameWithoutATripNumber() {
		Trip bus = TripBuilder.trip("bus").on(TestNetwork.line("31", "B", VehicleType.BUS))
				.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30"))
				.build();

		assertThat(bus.displayName()).isEqualTo("31");
	}

	@Test
	void extraTripsAreNotPlannedAndCancelledTripsDidntHappen() {
		Trip extra = TripBuilder.trip("x").extra()
				.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30")).build();
		Trip cancelled = TripBuilder.trip("c").cancelled()
				.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30")).build();

		assertThat(extra.existsOn(TimeBasis.SCHEDULED)).isFalse();
		assertThat(extra.existsOn(TimeBasis.ACTUAL)).isTrue();
		assertThat(cancelled.existsOn(TimeBasis.SCHEDULED)).isTrue();
		assertThat(cancelled.existsOn(TimeBasis.ACTUAL)).isFalse();
	}

	@Test
	void buildsEachTimelineOnce() {
		Trip ic712 = TestTrips.ic712();

		assertThat(ic712.timeline(TimeBasis.ACTUAL)).isSameAs(ic712.timeline(TimeBasis.ACTUAL));
		assertThat(ic712.timeline(TimeBasis.ACTUAL)).isNotSameAs(ic712.timeline(TimeBasis.SCHEDULED));
	}
}
