package ch.usi.smb_backend.service;

import static ch.usi.smb_backend.service.VehicleTrackingService.BASIS;

import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import java.util.List;
import org.springframework.stereotype.Service;

/** One trip at a time: its stops, its legs, and finding it by its number. */
@Service
public class TripService {

	private final TripRepository trips;
	private final VehicleFactory vehicles;

	public TripService(TripRepository trips, VehicleFactory vehicles) {
		this.trips = trips;
		this.vehicles = vehicles;
	}

	/**
	 * @throws NotFoundException when there is no such trip
	 */
	public Trip trip(TripKey key) {
		return trips.findByKey(key).orElseThrow(() -> new NotFoundException("no trip " + key));
	}

	/** Every leg of the trip, as it really ran; empty for a cancelled trip. */
	public List<Movement> movementsOf(TripKey key) {
		Trip trip = trip(key);
		var timeline = trip.timeline(BASIS);
		return vehicles.create(trip).movementsBetween(timeline.firstDeparture(), timeline.lastArrival(), BASIS);
	}

	/** The trips of that day with that number, by id; several when operators reuse a number. */
	public List<Trip> findByNumber(ServiceDay day, String number) {
		return trips.findByNumber(day, number).stream()
				.sorted(java.util.Comparator.comparing(trip -> trip.key().tripId()))
				.toList();
	}
}
