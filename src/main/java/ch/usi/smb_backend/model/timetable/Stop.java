package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.Objects;
import java.util.Optional;

/**
 * A point where the vehicle stops on one time basis, with its times on that basis already made
 * consistent by {@link TripTimeline}.
 */
public record Stop(StopEvent event, ServiceTime arrival, ServiceTime departure, TimeBasis basis) {

	public Stop {
		Objects.requireNonNull(event, "event");
		Objects.requireNonNull(arrival, "arrival");
		Objects.requireNonNull(departure, "departure");
		Objects.requireNonNull(basis, "basis");
		if (departure.isBefore(arrival)) {
			throw new IllegalArgumentException("a stop can't be left before it is reached: " + event.station());
		}
	}

	public Station station() {
		return event.station();
	}

	/** {@code arrival <= time <= departure}. */
	public boolean isAt(ServiceTime time) {
		return !time.isBefore(arrival) && !time.isAfter(departure);
	}

	/** The departure delay, or the arrival delay at a last stop. */
	public Optional<Delay> delay() {
		return event.departure().or(event::arrival).flatMap(EventTime::delay);
	}

	public boolean isTimeReliable() {
		return event.isReliableOn(basis);
	}
}
