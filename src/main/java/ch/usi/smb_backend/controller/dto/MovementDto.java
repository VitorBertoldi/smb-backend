package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.timetable.Leg;
import ch.usi.smb_backend.model.vehicle.MotionProfile;
import ch.usi.smb_backend.model.vehicle.Movement;
import java.util.List;

/**
 * One leg to animate, ids only: stations come from {@code /stations}, paths from {@code /paths},
 * and the trip's details from {@code /trips/{date}/{tripId}}, all fetched on demand. The service
 * date is the one asked for. The vehicle moves from {@code departS} to {@code arriveS} with the
 * named motion curve; an empty {@code pathIds} means a straight line between the two stations.
 */
public record MovementDto(
		String tripId,
		VehicleType vehicleType,
		List<Integer> pathIds,
		String fromStationId,
		String toStationId,
		int departS,
		int arriveS,
		MotionProfile motion,
		Integer delayS,
		boolean estimated) {

	public static MovementDto from(Movement movement) {
		Leg leg = movement.leg();
		return new MovementDto(
				movement.vehicle().trip().key().tripId(),
				movement.vehicle().trip().line().vehicleType(),
				leg.segments().stream().map(PathSegment::id).toList(),
				leg.from().station().id().code(),
				leg.to().station().id().code(),
				leg.from().departure().seconds(),
				leg.to().arrival().seconds(),
				movement.motionProfile(),
				leg.from().delay().map(Delay::seconds).orElse(null),
				leg.isEstimated());
	}
}
