package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.timetable.TripKey;
import java.time.LocalDate;

/** A trip's key: its service date and Istdaten trip id. */
public record TripRefDto(LocalDate date, String id) {

	public static TripRefDto from(TripKey key) {
		return new TripRefDto(key.serviceDate(), key.tripId());
	}
}
