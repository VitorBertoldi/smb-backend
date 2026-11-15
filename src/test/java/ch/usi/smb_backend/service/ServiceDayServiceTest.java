package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ch.usi.smb_backend.model.fixtures.TestTrips;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ServiceDayServiceTest {

	private final ServiceDayService serviceDays = new ServiceDayService(new InMemoryTripRepository(TestTrips.ic712()));

	@Test
	void acceptsADateWithTrips() {
		assertThat(serviceDays.require(WEDNESDAY.date())).isEqualTo(WEDNESDAY);
	}

	@Test
	void refusesADateWithoutTripsAndSaysWhichDatesExist() {
		assertThatExceptionOfType(UnknownServiceDateException.class)
				.isThrownBy(() -> serviceDays.require(LocalDate.of(2026, 12, 25)))
				.satisfies(unknown -> assertThat(unknown.availableDates()).containsExactly(WEDNESDAY.date()));
	}
}
