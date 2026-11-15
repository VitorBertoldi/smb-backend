package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeStatus;

/** One arrival or departure: planned and actual, as seconds and as wall-clock text. */
public record EventTimeDto(
		int scheduledS,
		String scheduled,
		Integer actualS,
		String actual,
		TimeStatus status,
		Integer delayS) {

	public static EventTimeDto from(EventTime time, ServiceDay day) {
		return new EventTimeDto(
				time.scheduled().seconds(),
				day.formatClock(time.scheduled()),
				time.actual().map(ServiceTime::seconds).orElse(null),
				time.actual().map(day::formatClock).orElse(null),
				time.status(),
				time.delay().map(Delay::seconds).orElse(null));
	}
}
