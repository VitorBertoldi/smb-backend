package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.vehicle.VehiclePosition;
import ch.usi.smb_backend.model.vehicle.VehicleStatus;

public record VehiclePositionDto(
		TripRefDto trip,
		String displayName,
		StationRefDto destination,
		VehicleType vehicleType,
		TimeBasis basis,
		VehicleStatus status,
		LocationDto location,
		StationRefDto station,
		StationRefDto previousStation,
		StationRefDto nextStation,
		Double legProgress,
		Integer delayS,
		boolean timeReliable,
		boolean estimated,
		Level underground) {

	public static VehiclePositionDto from(VehiclePosition position) {
		Trip trip = position.trip();
		return new VehiclePositionDto(
				TripRefDto.from(trip.key()),
				trip.displayName(),
				StationRefDto.from(trip.destination()),
				trip.line().vehicleType(),
				position.basis(),
				position.status(),
				position.location().map(LocationDto::from).orElse(null),
				position.station().map(StationRefDto::from).orElse(null),
				position.previousStation().map(StationRefDto::from).orElse(null),
				position.nextStation().map(StationRefDto::from).orElse(null),
				position.legProgress().isPresent() ? position.legProgress().getAsDouble() : null,
				position.delay().map(Delay::seconds).orElse(null),
				position.timeReliable(),
				position.estimated(),
				position.underground());
	}
}
