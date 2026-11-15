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
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import ch.usi.smb_backend.model.vehicle.VehiclePosition;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VehicleTrackingServiceTest {

	private final Trip ic712 = TestTrips.ic712();
	private final Trip bus = TripBuilder.trip("bus-31").on(TestNetwork.line("31", "B", VehicleType.BUS))
			.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:20"))
			.build();
	private final VehicleTrackingService tracking =
			new VehicleTrackingService(new InMemoryTripRepository(ic712, bus), new VehicleFactory());

	@Test
	void answersOnlyTheVehiclesRunningAtThatMoment() {
		List<VehiclePosition> atTen = tracking.positionsAt(WEDNESDAY, at("10:10"), TimeBasis.SCHEDULED, Set.of());
		List<VehiclePosition> atNoon = tracking.positionsAt(WEDNESDAY, at("12:00"), TimeBasis.SCHEDULED, Set.of());

		assertThat(atTen).extracting(VehiclePosition::trip).containsExactlyInAnyOrder(ic712, bus);
		assertThat(atNoon).isEmpty();
	}

	@Test
	void filtersByVehicleType() {
		List<VehiclePosition> buses = tracking.positionsAt(WEDNESDAY, at("10:10"), TimeBasis.SCHEDULED, Set.of(VehicleType.BUS));

		assertThat(buses).extracting(VehiclePosition::trip).containsExactly(bus);
	}

	@Test
	void listsTheMovementsOfAWindowByDeparture() {
		List<Movement> movements = tracking.movementsBetween(WEDNESDAY, at("09:00"), at("11:00"), TimeBasis.SCHEDULED, Set.of());

		assertThat(movements).extracting(movement -> movement.vehicle().trip()).containsExactly(ic712, bus);
	}
}
