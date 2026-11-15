package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Leg;
import ch.usi.smb_backend.model.timetable.Stop;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripTimeline;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A trip in motion: it knows how its kind of vehicle moves. Created from a trip when needed
 * ({@link VehicleFactory}), never stored. Subclasses exist only where the movement differs.
 */
public abstract sealed class Vehicle permits Train, Tram, Bus, Boat, Funicular, CableCar {

	private final Trip trip;

	protected Vehicle(Trip trip) {
		this.trip = Objects.requireNonNull(trip, "trip");
	}

	public Trip trip() {
		return trip;
	}

	/** How this kind of vehicle covers distance over time on a leg. */
	public abstract MotionProfile motionProfile();

	/** Template Method: the same steps for every vehicle; only the motion profile differs. */
	public final VehiclePosition positionAt(ServiceTime now, TimeBasis basis) {
		TripTimeline timeline = trip.timeline(basis);
		if (!timeline.isRunningAt(now)) {
			return VehiclePosition.notRunning(trip, basis);
		}
		Optional<Stop> stop = timeline.stopAt(now);
		if (stop.isPresent()) {
			return VehiclePosition.atStation(trip, basis, stop.get());
		}
		Leg leg = timeline.legAt(now).orElseThrow(); // running and not at a stop: must be moving
		double distanceFraction = motionProfile().apply(leg.timeFraction(now));
		return VehiclePosition.moving(trip, basis, leg, distanceFraction);
	}

	/** Every leg this vehicle moves on at some moment inside the window. */
	public final List<Movement> movementsBetween(ServiceTime windowStart, ServiceTime windowEnd, TimeBasis basis) {
		return trip.timeline(basis).legsMovingBetween(windowStart, windowEnd).stream()
				.map(leg -> new Movement(this, leg))
				.toList();
	}
}
