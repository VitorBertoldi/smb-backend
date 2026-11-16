package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.fixtures.TripBuilder;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

class TripServiceTest {

	private final Trip ic712 = TestTrips.ic712();
	private final Trip cancelled = TripBuilder.trip("cancelled").number("2512").cancelled()
			.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30"))
			.build();
	private final Trip sameNumber = TripBuilder.trip("other-operator").number("712")
			.stops(StopEventBuilder.at(AARAU).departs("11:00"), StopEventBuilder.at(BERN).arrives("11:30"))
			.build();
	private final TripService service =
			new TripService(new InMemoryTripRepository(ic712, cancelled, sameNumber), new VehicleFactory());

	@Test
	void findsATripByItsKey() {
		assertThat(service.trip(ic712.key())).isEqualTo(ic712);
	}

	@Test
	void saysSoWhenThereIsNoSuchTrip() {
		TripKey unknown = new TripKey(WEDNESDAY.date(), "nope");

		assertThatThrownBy(() -> service.trip(unknown)).isInstanceOf(NotFoundException.class).hasMessageContaining("nope");
	}

	@Test
	void givesEveryLegOfATripAsItRan() {
		List<Movement> movements = service.movementsOf(ic712.key());

		assertThat(movements).hasSize(1);
		assertThat(movements.getFirst().leg().from().departure()).isEqualTo(WEDNESDAY.parseClock("09:33:10"));
	}

	@Test
	void givesNoLegsForACancelledTrip() {
		assertThat(service.movementsOf(cancelled.key())).isEmpty();
	}

	@Test
	void findsEveryTripWithANumberOrderedById() {
		assertThat(service.findByNumber(WEDNESDAY, "712")).containsExactly(ic712, sameNumber);
		assertThat(service.findByNumber(WEDNESDAY, "1")).isEmpty();
	}
}
