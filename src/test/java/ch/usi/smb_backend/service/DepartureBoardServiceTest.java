package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.WEDNESDAY;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Departure;
import java.util.List;
import org.junit.jupiter.api.Test;

class DepartureBoardServiceTest {

	private final DepartureBoardService board = new DepartureBoardService(
			new InMemoryTripRepository(TestTrips.ic712(), TestTrips.withCancelledStopAtAarau()),
			new NetworkService(
					new InMemoryStationRepository(ZURICH_HB, AARAU, BERN), noLinesNeeded -> List.of(), noPathsNeeded -> List.of()));

	@Test
	void choosesTheWindowOnTheRequestedBasis() {
		// IC 712 was planned at 09:32 but left at 09:33:10.
		List<Departure> planned = board.departures(ZURICH_HB.id(), WEDNESDAY, at("09:30"), at("09:33"), TimeBasis.SCHEDULED);
		List<Departure> happened = board.departures(ZURICH_HB.id(), WEDNESDAY, at("09:30"), at("09:33"), TimeBasis.ACTUAL);

		assertThat(planned).extracting(departure -> departure.trip().displayName()).containsExactly("IC 712");
		assertThat(happened).isEmpty();
	}

	@Test
	void keepsCancelledDeparturesAndLeavesOutOtherStations() {
		List<Departure> aarau = board.departures(AARAU.id(), WEDNESDAY, at("09:00"), at("12:00"), TimeBasis.ACTUAL);

		assertThat(aarau).singleElement().satisfies(departure -> {
			assertThat(departure.station()).isEqualTo(AARAU);
			assertThat(departure.cancelled()).isTrue();
		});
	}

	@Test
	void refusesAnUnknownStation() {
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() ->
				board.departures(new StationId("0000000"), WEDNESDAY, at("09:00"), at("10:00"), TimeBasis.ACTUAL));
	}
}
