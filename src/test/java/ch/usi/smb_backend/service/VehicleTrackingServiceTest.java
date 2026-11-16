package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.WEDNESDAY;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestNetwork;
import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.fixtures.TripBuilder;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

class VehicleTrackingServiceTest {

	private final Trip ic712 = TestTrips.ic712();
	private final Trip bus = TripBuilder.trip("bus-31").on(TestNetwork.line("31", "B", VehicleType.BUS))
			.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:20"))
			.build();
	private final Trip cancelled = TripBuilder.trip("cancelled").cancelled()
			.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30"))
			.build();
	private final VehicleTrackingService tracking =
			new VehicleTrackingService(new InMemoryTripRepository(ic712, bus, cancelled), new VehicleFactory());

	@Test
	void listsTheMovementsOfAWindowByDeparture() {
		List<Movement> movements = tracking.movementsBetween(WEDNESDAY, at("09:00"), at("11:00"));

		assertThat(movements).extracting(movement -> movement.vehicle().trip()).containsExactly(ic712, bus);
	}

	@Test
	void usesTheActualTimes() {
		Movement movement = tracking.movementsBetween(WEDNESDAY, at("09:00"), at("11:00")).getFirst();

		assertThat(movement.leg().from().departure()).isEqualTo(at("09:33:10"));
		assertThat(movement.leg().to().arrival()).isEqualTo(at("10:30:40"));
	}

	@Test
	void showsALateTrainAfterItsPlannedArrival() {
		List<Movement> movements = tracking.movementsBetween(WEDNESDAY, at("10:29"), at("10:30"));

		assertThat(movements).extracting(movement -> movement.vehicle().trip()).containsExactly(ic712);
	}

	@Test
	void leavesOutWindowsWithNothingMoving() {
		assertThat(tracking.movementsBetween(WEDNESDAY, at("12:00"), at("13:00"))).isEmpty();
	}
}
