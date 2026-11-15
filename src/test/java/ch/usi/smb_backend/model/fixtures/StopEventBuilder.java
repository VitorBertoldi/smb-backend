package ch.usi.smb_backend.model.fixtures;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.TimeStatus;
import ch.usi.smb_backend.model.timetable.StopEvent;
import java.util.Optional;

/** Builds a {@link StopEvent} for tests, with clock times written as text. */
public final class StopEventBuilder {

	private final Station station;
	private Optional<EventTime> arrival = Optional.empty();
	private Optional<EventTime> departure = Optional.empty();
	private Optional<Platform> platform = Optional.empty();
	private Optional<PathSegment> nextSegment = Optional.empty();
	private boolean cancelled;
	private boolean passThrough;

	private StopEventBuilder(Station station) {
		this.station = station;
	}

	public static StopEventBuilder at(Station station) {
		return new StopEventBuilder(station);
	}

	/** Scheduled arrival, nothing measured. */
	public StopEventBuilder arrives(String scheduled) {
		arrival = Optional.of(EventTime.scheduledOnly(TestNetwork.at(scheduled)));
		return this;
	}

	public StopEventBuilder arrives(String scheduled, String actual, TimeStatus status) {
		arrival = Optional.of(EventTime.of(TestNetwork.at(scheduled), TestNetwork.at(actual), status));
		return this;
	}

	/** Scheduled departure, nothing measured. */
	public StopEventBuilder departs(String scheduled) {
		departure = Optional.of(EventTime.scheduledOnly(TestNetwork.at(scheduled)));
		return this;
	}

	public StopEventBuilder departs(String scheduled, String actual, TimeStatus status) {
		departure = Optional.of(EventTime.of(TestNetwork.at(scheduled), TestNetwork.at(actual), status));
		return this;
	}

	public StopEventBuilder onPlatform(String sloid) {
		platform = Optional.of(new Platform(sloid));
		return this;
	}

	public StopEventBuilder then(PathSegment segment) {
		nextSegment = Optional.of(segment);
		return this;
	}

	public StopEventBuilder cancelled() {
		cancelled = true;
		return this;
	}

	public StopEventBuilder passThrough() {
		passThrough = true;
		return this;
	}

	StopEvent build(int sequence) {
		return new StopEvent(sequence, station, platform, arrival, departure, cancelled, passThrough, nextSegment);
	}

	/** For tests that check a single stop event's own rules. */
	public StopEvent buildAlone() {
		return build(1);
	}
}
