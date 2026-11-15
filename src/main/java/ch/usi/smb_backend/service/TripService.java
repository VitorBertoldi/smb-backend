package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import ch.usi.smb_backend.model.vehicle.VehiclePosition;
import org.springframework.stereotype.Service;

/** One trip: its stop events, and where its vehicle is. */
@Service
public class TripService {

	private final TripRepository trips;
	private final VehicleFactory vehicles;

	public TripService(TripRepository trips, VehicleFactory vehicles) {
		this.trips = trips;
		this.vehicles = vehicles;
	}

	/**
	 * @throws NotFoundException when there's no such trip
	 */
	public Trip trip(TripKey key) {
		return trips.findByKey(key).orElseThrow(() -> new NotFoundException("no trip " + key));
	}

	/**
	 * @throws NotFoundException when there's no such trip
	 */
	public VehiclePosition position(TripKey key, ServiceTime time, TimeBasis basis) {
		return vehicles.create(trip(key)).positionAt(time, basis);
	}
}
