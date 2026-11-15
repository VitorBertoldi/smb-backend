package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.TimeStatus;
import ch.usi.smb_backend.model.timetable.StopEvent;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.repository.entity.StopEventEntity;
import ch.usi.smb_backend.repository.entity.TripEntity;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Turns a trip row and its stop event rows into a {@link Trip}. Timestamps become service times
 * here, and only here. Throws {@link IllegalArgumentException} when the rows break a model rule.
 */
final class TripMapper {

	private TripMapper() {
	}

	static Trip toTrip(TripEntity row, ReferenceData reference) {
		ServiceDay day = ServiceDay.of(row.getId().serviceDate());
		return new Trip(
				new TripKey(row.getId().serviceDate(), row.getId().tripId()),
				reference.line(row.getLineId()),
				Optional.ofNullable(row.getTripNumber()),
				row.isCancelled(),
				row.isExtraTrip(),
				row.getStopEvents().stream().map(event -> toStopEvent(event, day, reference)).toList());
	}

	private static StopEvent toStopEvent(StopEventEntity row, ServiceDay day, ReferenceData reference) {
		return new StopEvent(
				row.getId().stopSequence(),
				reference.station(new StationId(row.getStationId())),
				Optional.ofNullable(row.getPlatformSloid()).map(Platform::new),
				toEventTime(row.getScheduledArrival(), row.getActualArrival(), row.getArrivalStatus(), day),
				toEventTime(row.getScheduledDeparture(), row.getActualDeparture(), row.getDepartureStatus(), day),
				row.isStopCancelled(),
				row.isPassThrough(),
				Optional.ofNullable(row.getNextSegmentId()).map(reference::segment));
	}

	/** Empty when nothing was planned; an actual time without a known status counts as a forecast. */
	private static Optional<EventTime> toEventTime(
			OffsetDateTime scheduled, OffsetDateTime actual, String status, ServiceDay day) {
		if (scheduled == null) {
			return Optional.empty();
		}
		if (actual == null) {
			return Optional.of(EventTime.scheduledOnly(day.toServiceTime(scheduled.toInstant())));
		}
		TimeStatus known = toTimeStatus(status);
		return Optional.of(EventTime.of(
				day.toServiceTime(scheduled.toInstant()),
				day.toServiceTime(actual.toInstant()),
				known == TimeStatus.UNKNOWN ? TimeStatus.FORECAST : known));
	}

	private static TimeStatus toTimeStatus(String istdatenStatus) {
		if (istdatenStatus == null) {
			return TimeStatus.UNKNOWN;
		}
		return switch (istdatenStatus) {
			case "REAL" -> TimeStatus.MEASURED;
			case "GESCHAETZT" -> TimeStatus.ESTIMATED;
			case "PROGNOSE" -> TimeStatus.FORECAST;
			default -> TimeStatus.UNKNOWN;
		};
	}
}
