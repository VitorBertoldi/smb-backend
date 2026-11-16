package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.timetable.StopEvent;
import java.util.Optional;

/**
 * One stop event of a trip. Times are service-time seconds (since the service day's start); the
 * first stop has no arrival and the last none departure, and an actual time may be missing.
 */
public record StopDto(
		int seq,
		String stationId,
		Integer scheduledArriveS,
		Integer actualArriveS,
		Integer scheduledDepartS,
		Integer actualDepartS,
		boolean cancelled,
		boolean passThrough) {

	public static StopDto from(StopEvent event) {
		return new StopDto(
				event.sequence(),
				event.station().id().code(),
				scheduled(event.arrival()),
				actual(event.arrival()),
				scheduled(event.departure()),
				actual(event.departure()),
				event.cancelled(),
				event.passThrough());
	}

	private static Integer scheduled(Optional<EventTime> time) {
		return time.map(value -> value.scheduled().seconds()).orElse(null);
	}

	private static Integer actual(Optional<EventTime> time) {
		return time.flatMap(EventTime::actual).map(value -> value.seconds()).orElse(null);
	}
}
