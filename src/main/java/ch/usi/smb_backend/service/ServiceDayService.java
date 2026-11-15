package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** Which service days the database holds. */
@Service
public class ServiceDayService {

	private final TripRepository trips;

	public ServiceDayService(TripRepository trips) {
		this.trips = trips;
	}

	public List<LocalDate> availableDates() {
		return trips.serviceDates();
	}

	/**
	 * @throws UnknownServiceDateException when the database has no trips on that date
	 */
	public ServiceDay require(LocalDate date) {
		List<LocalDate> available = availableDates();
		if (!available.contains(date)) {
			throw new UnknownServiceDateException(date, available);
		}
		return ServiceDay.of(date);
	}
}
