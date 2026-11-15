package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.timetable.StopEvent;

public record StopEventDto(
		int sequence,
		StationRefDto station,
		String platform,
		EventTimeDto arrival,
		EventTimeDto departure,
		boolean cancelled,
		boolean passThrough,
		Integer nextPathId) {

	public static StopEventDto from(StopEvent event, ServiceDay day) {
		return new StopEventDto(
				event.sequence(),
				StationRefDto.from(event.station()),
				event.platform().map(Platform::label).orElse(null),
				event.arrival().map(time -> EventTimeDto.from(time, day)).orElse(null),
				event.departure().map(time -> EventTimeDto.from(time, day)).orElse(null),
				event.cancelled(),
				event.passThrough(),
				event.nextSegment().map(PathSegment::id).orElse(null));
	}
}
