package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Leg;
import ch.usi.smb_backend.model.timetable.Stop;
import ch.usi.smb_backend.model.timetable.Trip;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Where a vehicle is at one moment, on one time basis. Built only through its three factory
 * methods, so the fields always fit the status.
 *
 * @param legProgress   share of the leg's distance covered, while moving
 * @param timeReliable  the times behind this position can be trusted on its basis
 * @param estimated     the position lies on a straight line, not a real path
 */
public record VehiclePosition(
		Trip trip,
		TimeBasis basis,
		VehicleStatus status,
		Optional<GeoPoint> location,
		Optional<Station> station,
		Optional<Station> previousStation,
		Optional<Station> nextStation,
		OptionalDouble legProgress,
		Optional<Delay> delay,
		boolean timeReliable,
		boolean estimated,
		Level underground) {

	public VehiclePosition {
		Objects.requireNonNull(trip, "trip");
		Objects.requireNonNull(basis, "basis");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(underground, "underground");
	}

	static VehiclePosition notRunning(Trip trip, TimeBasis basis) {
		return new VehiclePosition(trip, basis, VehicleStatus.NOT_RUNNING,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				OptionalDouble.empty(), Optional.empty(), false, false, Level.UNKNOWN);
	}

	static VehiclePosition atStation(Trip trip, TimeBasis basis, Stop stop) {
		Station station = stop.station();
		return new VehiclePosition(trip, basis, VehicleStatus.AT_STATION,
				Optional.of(station.location()), Optional.of(station), Optional.empty(), Optional.empty(),
				OptionalDouble.empty(), stop.delay(), stop.isTimeReliable(), false, station.level());
	}

	static VehiclePosition moving(Trip trip, TimeBasis basis, Leg leg, double distanceFraction) {
		boolean reliable = leg.from().isTimeReliable() && leg.to().isTimeReliable();
		Level underground = leg.mayBeInTunnel() ? Level.UNKNOWN : Level.SURFACE;
		return new VehiclePosition(trip, basis, VehicleStatus.MOVING,
				Optional.of(leg.pointAt(distanceFraction)), Optional.empty(),
				Optional.of(leg.from().station()), Optional.of(leg.to().station()),
				OptionalDouble.of(distanceFraction), leg.from().delay(), reliable, leg.isEstimated(), underground);
	}
}
