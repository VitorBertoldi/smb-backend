package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One run on one service date: the aggregate root, and the only way to reach its stop events. A
 * trip holds facts only; {@link #timeline(TimeBasis)} interprets them. Equal by key.
 *
 * <p>Checked when built: at least two stop events, sequences increasing, scheduled times never
 * going backwards, and every path segment leading to the next stop event's station.
 */
public final class Trip {

	private static final int MINIMUM_STOP_EVENTS = 2;

	private final TripKey key;
	private final Line line;
	private final Optional<String> tripNumber;
	private final boolean cancelled;
	private final boolean extra;
	private final List<StopEvent> stopEvents;
	private final Map<TimeBasis, TripTimeline> timelines = new ConcurrentHashMap<>();

	/**
	 * @param cancelled the whole trip didn't run
	 * @param extra     added on the day, not in the timetable
	 */
	public Trip(
			TripKey key,
			Line line,
			Optional<String> tripNumber,
			boolean cancelled,
			boolean extra,
			List<StopEvent> stopEvents) {
		this.key = Objects.requireNonNull(key, "key");
		this.line = Objects.requireNonNull(line, "line");
		this.tripNumber = Objects.requireNonNull(tripNumber, "tripNumber");
		this.cancelled = cancelled;
		this.extra = extra;
		this.stopEvents = List.copyOf(stopEvents);
		requireValidStopEvents();
	}

	public TripKey key() {
		return key;
	}

	public Line line() {
		return line;
	}

	public Optional<String> tripNumber() {
		return tripNumber;
	}

	public boolean cancelled() {
		return cancelled;
	}

	public boolean extra() {
		return extra;
	}

	public List<StopEvent> stopEvents() {
		return stopEvents;
	}

	public Station origin() {
		return stopEvents.getFirst().station();
	}

	/** The last stop's station; Istdaten has no headsign. */
	public Station destination() {
		return stopEvents.getLast().station();
	}

	/** {@code IC 712}; the line name when there's no trip number. */
	public String displayName() {
		return tripNumber.map(number -> line.category() + " " + number).orElse(line.name());
	}

	/** The plan has no extra trips; reality has no cancelled ones. */
	public boolean existsOn(TimeBasis basis) {
		return switch (basis) {
			case SCHEDULED -> !extra;
			case ACTUAL -> !cancelled;
		};
	}

	/** The trip as it runs on that basis; built once per basis. */
	public TripTimeline timeline(TimeBasis basis) {
		return timelines.computeIfAbsent(basis, chosen -> TripTimeline.of(this, chosen));
	}

	private void requireValidStopEvents() {
		if (stopEvents.size() < MINIMUM_STOP_EVENTS) {
			throw invalid("needs at least " + MINIMUM_STOP_EVENTS + " stop events, has " + stopEvents.size());
		}
		for (int i = 1; i < stopEvents.size(); i++) {
			requireValidHop(stopEvents.get(i - 1), stopEvents.get(i));
		}
		if (stopEvents.getLast().nextSegment().isPresent()) {
			throw invalid("has a path segment after its last stop");
		}
	}

	private void requireValidHop(StopEvent previous, StopEvent next) {
		if (next.sequence() <= previous.sequence()) {
			throw invalid("sequence goes from " + previous.sequence() + " to " + next.sequence());
		}
		if (next.arrivalOn(TimeBasis.SCHEDULED).isBefore(previous.departureOn(TimeBasis.SCHEDULED))) {
			throw invalid("scheduled time goes backwards from " + previous.station() + " to " + next.station());
		}
		Optional<PathSegment> segment = previous.nextSegment();
		if (segment.isPresent() && !segment.get().connects(previous.station(), next.station())) {
			throw invalid(segment.get() + " doesn't lead from " + previous.station() + " to " + next.station());
		}
	}

	private IllegalArgumentException invalid(String problem) {
		return new IllegalArgumentException("trip " + key + " " + problem);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Trip trip && key.equals(trip.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		return displayName() + " " + key;
	}
}
