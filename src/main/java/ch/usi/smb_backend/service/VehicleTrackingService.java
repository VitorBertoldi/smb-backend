package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.vehicle.Movement;
import ch.usi.smb_backend.model.vehicle.Vehicle;
import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import ch.usi.smb_backend.model.vehicle.VehiclePosition;
import ch.usi.smb_backend.model.vehicle.VehicleStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/** Every vehicle at once: positions at a moment, and movements through a window for animation. */
@Service
public class VehicleTrackingService {

	private final TripRepository trips;
	private final VehicleFactory vehicles;

	public VehicleTrackingService(TripRepository trips, VehicleFactory vehicles) {
		this.trips = trips;
		this.vehicles = vehicles;
	}

	/**
	 * Every vehicle running at that moment, on that basis.
	 *
	 * @param types the vehicle types wanted; empty for all
	 */
	public List<VehiclePosition> positionsAt(ServiceDay day, ServiceTime time, TimeBasis basis, Set<VehicleType> types) {
		return vehiclesOf(trips.findCandidatesRunningBetween(day, time, time), types)
				.map(vehicle -> vehicle.positionAt(time, basis))
				.filter(position -> position.status() != VehicleStatus.NOT_RUNNING)
				.toList();
	}

	/**
	 * Every leg moving at some moment of the window, ordered by departure.
	 *
	 * @param types the vehicle types wanted; empty for all
	 */
	public List<Movement> movementsBetween(
			ServiceDay day, ServiceTime from, ServiceTime to, TimeBasis basis, Set<VehicleType> types) {
		return vehiclesOf(trips.findCandidatesRunningBetween(day, from, to), types)
				.flatMap(vehicle -> vehicle.movementsBetween(from, to, basis).stream())
				.sorted(Comparator.comparing(movement -> movement.leg().from().departure()))
				.toList();
	}

	private Stream<Vehicle> vehiclesOf(List<Trip> candidates, Set<VehicleType> types) {
		return candidates.stream()
				.filter(trip -> types.isEmpty() || types.contains(trip.line().vehicleType()))
				.map(vehicles::create);
	}
}
