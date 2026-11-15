package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.WEDNESDAY;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import ch.usi.smb_backend.model.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;

class TripServiceTest {

	private final Trip ic712 = TestTrips.ic712();
	private final TripService trips = new TripService(new InMemoryTripRepository(ic712), new VehicleFactory());

	@Test
	void findsATripAndItsPosition() {
		assertThat(trips.trip(ic712.key())).isEqualTo(ic712);
		assertThat(trips.position(ic712.key(), at("09:33:10"), TimeBasis.ACTUAL).station()).contains(ZURICH_HB);
		assertThat(trips.position(ic712.key(), at("10:00"), TimeBasis.ACTUAL).status()).isEqualTo(VehicleStatus.MOVING);
	}

	@Test
	void refusesAnUnknownTrip() {
		assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> trips.trip(new TripKey(WEDNESDAY.date(), "nope")))
				.withMessageContaining("nope");
	}
}
