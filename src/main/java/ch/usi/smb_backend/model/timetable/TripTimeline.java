package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * A trip as it runs on one time basis: the stops it makes and the legs between them. Answers
 * every "where and when" question about the trip.
 *
 * <p>Actual times are measurements, so some come out of order. The timeline interprets them
 * without changing the data: going down the stops, each time is raised to at least the one
 * before, so a vehicle never moves backwards in time.
 */
public final class TripTimeline {

	private final TimeBasis basis;
	private final boolean tripExists;
	private final List<Stop> stops;
	private final List<Leg> legs;
	private final int[] arrivalSeconds;
	private final int[] departureSeconds;

	private TripTimeline(TimeBasis basis, boolean tripExists, List<Stop> stops, List<Leg> legs) {
		this.basis = basis;
		this.tripExists = tripExists;
		this.stops = List.copyOf(stops);
		this.legs = List.copyOf(legs);
		this.arrivalSeconds = stops.stream().mapToInt(stop -> stop.arrival().seconds()).toArray();
		this.departureSeconds = stops.stream().mapToInt(stop -> stop.departure().seconds()).toArray();
	}

	/** Builds the timeline of a valid trip; see {@link Trip#timeline(TimeBasis)}. */
	static TripTimeline of(Trip trip, TimeBasis basis) {
		List<StopEvent> events = trip.stopEvents();
		List<Integer> stopIndexes = stopIndexes(events, basis);
		List<Stop> stops = consistentStops(events, stopIndexes, basis);
		List<Leg> legs = new ArrayList<>();
		for (int i = 0; i + 1 < stops.size(); i++) {
			List<PathSegment> hops = hops(events, stopIndexes.get(i), stopIndexes.get(i + 1));
			legs.add(new Leg(stops.get(i), stops.get(i + 1), hops));
		}
		return new TripTimeline(basis, trip.existsOn(basis), stops, legs);
	}

	public TimeBasis basis() {
		return basis;
	}

	public List<Stop> stops() {
		return stops;
	}

	public List<Leg> legs() {
		return legs;
	}

	public ServiceTime firstDeparture() {
		return stops.getFirst().departure();
	}

	public ServiceTime lastArrival() {
		return stops.getLast().arrival();
	}

	/** The trip exists on this basis and {@code first departure <= time <= last arrival}. */
	public boolean isRunningAt(ServiceTime time) {
		return tripExists && !time.isBefore(firstDeparture()) && !time.isAfter(lastArrival());
	}

	/** The stop the vehicle is at; the last one when several match (legs that take no time). */
	public Optional<Stop> stopAt(ServiceTime time) {
		if (!isRunningAt(time)) {
			return Optional.empty();
		}
		int index = lastIndexAtOrBefore(arrivalSeconds, time.seconds());
		return index >= 0 && stops.get(index).isAt(time) ? Optional.of(stops.get(index)) : Optional.empty();
	}

	/** The leg the vehicle is moving on. */
	public Optional<Leg> legAt(ServiceTime time) {
		if (!isRunningAt(time)) {
			return Optional.empty();
		}
		int index = lastIndexAtOrBefore(departureSeconds, time.seconds() - 1);
		boolean onALeg = index >= 0 && index < legs.size() && legs.get(index).isMovingAt(time);
		return onALeg ? Optional.of(legs.get(index)) : Optional.empty();
	}

	/** The next stop the vehicle will reach after {@code time}. */
	public Optional<Stop> nextStopAfter(ServiceTime time) {
		int index = lastIndexAtOrBefore(arrivalSeconds, time.seconds()) + 1;
		return index < stops.size() ? Optional.of(stops.get(index)) : Optional.empty();
	}

	/** The legs on which the vehicle moves at some moment inside the window. */
	public List<Leg> legsMovingBetween(ServiceTime windowStart, ServiceTime windowEnd) {
		if (!tripExists) {
			return List.of();
		}
		return legs.stream().filter(leg -> leg.isMovingBetween(windowStart, windowEnd)).toList();
	}

	/** The events where the vehicle stops; the first and last always count, so the trip keeps its ends. */
	private static List<Integer> stopIndexes(List<StopEvent> events, TimeBasis basis) {
		int last = events.size() - 1;
		return IntStream.rangeClosed(0, last)
				.filter(i -> i == 0 || i == last || events.get(i).stopsOn(basis))
				.boxed()
				.toList();
	}

	private static List<Stop> consistentStops(List<StopEvent> events, List<Integer> stopIndexes, TimeBasis basis) {
		List<Stop> stops = new ArrayList<>();
		ServiceTime previous = ServiceTime.START_OF_DAY;
		for (int index : stopIndexes) {
			StopEvent event = events.get(index);
			ServiceTime arrival = event.arrivalOn(basis).max(previous);
			ServiceTime departure = event.departureOn(basis).max(arrival);
			stops.add(new Stop(event, arrival, departure, basis));
			previous = departure;
		}
		return stops;
	}

	/** The path of every hop from one stop to the next; empty if any hop has none. */
	private static List<PathSegment> hops(List<StopEvent> events, int fromIndex, int toIndex) {
		List<Optional<PathSegment>> hops = events.subList(fromIndex, toIndex).stream()
				.map(StopEvent::nextSegment)
				.toList();
		boolean complete = hops.stream().allMatch(Optional::isPresent);
		return complete ? hops.stream().map(Optional::orElseThrow).toList() : List.of();
	}

	/** The largest index whose value is at most {@code limit}, or -1; {@code sorted} never decreases. */
	private static int lastIndexAtOrBefore(int[] sorted, int limit) {
		int low = 0;
		int high = sorted.length - 1;
		int found = -1;
		while (low <= high) {
			int middle = (low + high) >>> 1;
			if (sorted[middle] <= limit) {
				found = middle;
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		return found;
	}
}
