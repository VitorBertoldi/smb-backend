package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.Objects;
import java.util.Optional;

/**
 * A trip at one station: planned and actual arrival and departure, and what happened there. The
 * first stop of a trip has no arrival and the last one no departure.
 *
 * @param passThrough the vehicle passes the station without stopping
 * @param nextSegment the path to the trip's next stop event; empty at the last one, or when the
 *                    data has no path for the hop
 */
public record StopEvent(
		int sequence,
		Station station,
		Optional<Platform> platform,
		Optional<EventTime> arrival,
		Optional<EventTime> departure,
		boolean cancelled,
		boolean passThrough,
		Optional<PathSegment> nextSegment) {

	public StopEvent {
		Objects.requireNonNull(station, "station");
		Objects.requireNonNull(platform, "platform");
		Objects.requireNonNull(arrival, "arrival");
		Objects.requireNonNull(departure, "departure");
		Objects.requireNonNull(nextSegment, "nextSegment");
		if (arrival.isEmpty() && departure.isEmpty()) {
			throw new IllegalArgumentException("stop event " + sequence + " at " + station + " has no time at all");
		}
		boolean leavesBeforeArriving = arrival.isPresent() && departure.isPresent()
				&& arrival.get().scheduled().isAfter(departure.get().scheduled());
		if (leavesBeforeArriving) {
			throw new IllegalArgumentException(
					"stop event " + sequence + " at " + station + " is scheduled to leave before it arrives");
		}
	}

	/** Whether the vehicle stops here on that basis: never when passing; on ACTUAL not when cancelled. */
	public boolean stopsOn(TimeBasis basis) {
		return !passThrough && !(basis == TimeBasis.ACTUAL && cancelled);
	}

	/** The arrival on that basis; at the first stop, which has none, the departure. */
	public ServiceTime arrivalOn(TimeBasis basis) {
		return arrival.or(() -> departure).orElseThrow().on(basis);
	}

	/** The departure on that basis; at the last stop, which has none, the arrival. */
	public ServiceTime departureOn(TimeBasis basis) {
		return departure.or(() -> arrival).orElseThrow().on(basis);
	}

	/** Whether every time this event has can be trusted on that basis. */
	public boolean isReliableOn(TimeBasis basis) {
		return arrival.map(time -> time.isReliableOn(basis)).orElse(true)
				&& departure.map(time -> time.isReliableOn(basis)).orElse(true);
	}
}
