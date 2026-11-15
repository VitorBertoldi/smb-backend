package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * A fake that returns every trip of the day as a candidate: the services must then decide
 * exactly, which is what these tests check.
 */
class InMemoryTripRepository implements TripRepository {

	private final List<Trip> trips;

	InMemoryTripRepository(Trip... trips) {
		this.trips = List.of(trips);
	}

	@Override
	public List<LocalDate> serviceDates() {
		return trips.stream().map(trip -> trip.key().serviceDate()).distinct().sorted().toList();
	}

	@Override
	public Optional<Trip> findByKey(TripKey key) {
		return trips.stream().filter(trip -> trip.key().equals(key)).findFirst();
	}

	@Override
	public List<Trip> findCandidatesRunningBetween(ServiceDay day, ServiceTime from, ServiceTime to) {
		return tripsOn(day);
	}

	@Override
	public List<Trip> findCandidatesDepartingBetween(StationId station, ServiceDay day, ServiceTime from, ServiceTime to) {
		return tripsOn(day);
	}

	private List<Trip> tripsOn(ServiceDay day) {
		return trips.stream().filter(trip -> trip.key().serviceDate().equals(day.date())).toList();
	}
}
