package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.timetable.Departure;

/** One line of a departure board. */
public record DepartureDto(
		TripRefDto trip,
		String displayName,
		StationRefDto destination,
		String platform,
		EventTimeDto departure,
		boolean cancelled) {

	public static DepartureDto from(Departure departure, ServiceDay day) {
		return new DepartureDto(
				TripRefDto.from(departure.trip().key()),
				departure.trip().displayName(),
				StationRefDto.from(departure.destination()),
				departure.platform().map(Platform::label).orElse(null),
				EventTimeDto.from(departure.time(), day),
				departure.cancelled());
	}
}
