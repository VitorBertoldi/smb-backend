package ch.usi.smb_backend.model.fixtures;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.timetable.StopEvent;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Builds a {@link Trip} for tests; stop events get sequences 1, 2, 3, … in the order given. */
public final class TripBuilder {

	private final String tripId;
	private Line line = TestNetwork.intercity();
	private Optional<String> tripNumber = Optional.empty();
	private boolean cancelled;
	private boolean extra;
	private final List<StopEventBuilder> stopEvents = new ArrayList<>();

	private TripBuilder(String tripId) {
		this.tripId = tripId;
	}

	public static TripBuilder trip(String tripId) {
		return new TripBuilder(tripId);
	}

	public TripBuilder on(Line chosenLine) {
		line = chosenLine;
		return this;
	}

	public TripBuilder number(String number) {
		tripNumber = Optional.of(number);
		return this;
	}

	public TripBuilder cancelled() {
		cancelled = true;
		return this;
	}

	public TripBuilder extra() {
		extra = true;
		return this;
	}

	public TripBuilder stops(StopEventBuilder... events) {
		stopEvents.addAll(List.of(events));
		return this;
	}

	public Trip build() {
		List<StopEvent> built = new ArrayList<>();
		for (int i = 0; i < stopEvents.size(); i++) {
			built.add(stopEvents.get(i).build(i + 1));
		}
		TripKey key = new TripKey(TestNetwork.WEDNESDAY.date(), tripId);
		return new Trip(key, line, tripNumber, cancelled, extra, built);
	}
}
