package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.Objects;
import java.util.Optional;

/**
 * One line on a departure board. Only a stop event the vehicle leaves from qualifies: not the last
 * stop, not a pass-through. Cancelled departures stay on the board, marked, as on a real one.
 */
public record Departure(Trip trip, StopEvent event) {

	public Departure {
		Objects.requireNonNull(trip, "trip");
		Objects.requireNonNull(event, "event");
		if (!isDeparture(event)) {
			throw new IllegalArgumentException("nothing departs from " + event.station() + " on trip " + trip.key());
		}
	}

	/** Whether a stop event is a departure: it has a departure time and the vehicle stops there. */
	public static boolean isDeparture(StopEvent event) {
		return event.departure().isPresent() && !event.passThrough();
	}

	public Station station() {
		return event.station();
	}

	public Optional<Platform> platform() {
		return event.platform();
	}

	public Station destination() {
		return trip.destination();
	}

	public EventTime time() {
		return event.departure().orElseThrow();
	}

	public ServiceTime timeOn(TimeBasis basis) {
		return time().on(basis);
	}

	public boolean cancelled() {
		return trip.cancelled() || event.cancelled();
	}
}
