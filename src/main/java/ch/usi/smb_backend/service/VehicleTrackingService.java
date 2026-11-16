package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/** Every vehicle at once: what moves through a window, for the frontend to animate. */
@Service
public class VehicleTrackingService {

	/** Version 1 shows the day as it really ran; the timetable view comes in version 2. */
	private static final TimeBasis BASIS = TimeBasis.ACTUAL;

	private final TripRepository trips;
	private final VehicleFactory vehicles;

	public VehicleTrackingService(TripRepository trips, VehicleFactory vehicles) {
		this.trips = trips;
		this.vehicles = vehicles;
	}

	/** Every leg moving at some moment of the window, ordered by departure. */
	public List<Movement> movementsBetween(ServiceDay day, ServiceTime from, ServiceTime to) {
		return trips.findCandidatesRunningBetween(day, from, to).stream()
				.map(vehicles::create)
				.flatMap(vehicle -> vehicle.movementsBetween(from, to, BASIS).stream())
				.sorted(Comparator.comparing(movement -> movement.leg().from().departure()))
				.toList();
	}
}
