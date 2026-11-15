package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.timetable.Trip;
import java.util.List;

public record TripDto(
		TripRefDto trip,
		String displayName,
		LineDto line,
		StationRefDto origin,
		StationRefDto destination,
		boolean cancelled,
		boolean extra,
		List<StopEventDto> stopEvents) {

	public static TripDto from(Trip trip) {
		ServiceDay day = ServiceDay.of(trip.key().serviceDate());
		return new TripDto(
				TripRefDto.from(trip.key()),
				trip.displayName(),
				LineDto.from(trip.line()),
				StationRefDto.from(trip.origin()),
				StationRefDto.from(trip.destination()),
				trip.cancelled(),
				trip.extra(),
				trip.stopEvents().stream().map(event -> StopEventDto.from(event, day)).toList());
	}
}
