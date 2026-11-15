package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.timetable.Leg;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.vehicle.MotionProfile;
import ch.usi.smb_backend.model.vehicle.Movement;
import java.util.List;

/**
 * One leg to animate. The frontend fetches the paths once ({@code /paths?ids=...}) and moves the
 * vehicle along them from {@code departS} to {@code arriveS} with the named motion curve. An empty
 * {@code pathIds} means a straight line between the two stations.
 */
public record MovementDto(
		TripRefDto trip,
		String displayName,
		VehicleType vehicleType,
		List<Integer> pathIds,
		StationRefDto fromStation,
		StationRefDto toStation,
		int departS,
		int arriveS,
		MotionProfile motion,
		Integer delayS,
		boolean estimated) {

	public static MovementDto from(Movement movement) {
		Trip trip = movement.vehicle().trip();
		Leg leg = movement.leg();
		return new MovementDto(
				TripRefDto.from(trip.key()),
				trip.displayName(),
				trip.line().vehicleType(),
				leg.segments().stream().map(PathSegment::id).toList(),
				StationRefDto.from(leg.from().station()),
				StationRefDto.from(leg.to().station()),
				leg.from().departure().seconds(),
				leg.to().arrival().seconds(),
				movement.motionProfile(),
				leg.from().delay().map(Delay::seconds).orElse(null),
				leg.isEstimated());
	}
}
