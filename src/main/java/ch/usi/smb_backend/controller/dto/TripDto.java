package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.timetable.Trip;
import java.time.LocalDate;
import java.util.List;

/** One trip with all its stop events; stations are ids, to look up in {@code /stations}. */
public record TripDto(
		LocalDate date,
		String id,
		String number,
		String name,
		VehicleType vehicleType,
		boolean cancelled,
		boolean extra,
		List<StopDto> stops) {

	public static TripDto from(Trip trip) {
		return new TripDto(
				trip.key().serviceDate(),
				trip.key().tripId(),
				trip.tripNumber().orElse(null),
				trip.displayName(),
				trip.line().vehicleType(),
				trip.cancelled(),
				trip.extra(),
				trip.stopEvents().stream().map(StopDto::from).toList());
	}
}
