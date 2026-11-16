package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.timetable.Trip;
import java.time.LocalDate;

/** A trip found by number: enough to pick one, then fetch it by id. */
public record TripSummaryDto(LocalDate date, String id, String number, String name, VehicleType vehicleType) {

	public static TripSummaryDto from(Trip trip) {
		return new TripSummaryDto(
				trip.key().serviceDate(),
				trip.key().tripId(),
				trip.tripNumber().orElse(null),
				trip.displayName(),
				trip.line().vehicleType());
	}
}
