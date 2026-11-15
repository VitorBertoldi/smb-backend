package ch.usi.smb_backend.model.timetable;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.geo.Polyline;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.time.ServiceTime;
import java.util.List;
import java.util.Objects;

/**
 * The movement between two consecutive stops. It follows one path segment per hop, including the
 * hops through stations passed without stopping; with no path it is a straight line.
 */
public final class Leg {

	private final Stop from;
	private final Stop to;
	private final List<PathSegment> segments;
	private final Polyline path;

	/**
	 * @param segments the hops from {@code from} to {@code to} in order, or empty when any hop has
	 *                 no path
	 */
	public Leg(Stop from, Stop to, List<PathSegment> segments) {
		this.from = Objects.requireNonNull(from, "from");
		this.to = Objects.requireNonNull(to, "to");
		this.segments = List.copyOf(segments);
		if (to.arrival().isBefore(from.departure())) {
			throw new IllegalArgumentException("a leg can't arrive before it leaves: " + from.station() + " -> " + to.station());
		}
		this.path = this.segments.isEmpty()
				? Polyline.straight(from.station().location(), to.station().location())
				: joined(this.segments);
	}

	public Stop from() {
		return from;
	}

	public Stop to() {
		return to;
	}

	public List<PathSegment> segments() {
		return segments;
	}

	public Polyline path() {
		return path;
	}

	/** Strictly between leaving and arriving: the two ends belong to the stations. */
	public boolean isMovingAt(ServiceTime time) {
		return time.isAfter(from.departure()) && time.isBefore(to.arrival());
	}

	/** Whether the vehicle is on this leg at some moment strictly inside the window. */
	public boolean isMovingBetween(ServiceTime windowStart, ServiceTime windowEnd) {
		return from.departure().isBefore(windowEnd) && to.arrival().isAfter(windowStart)
				&& durationSeconds() > 0;
	}

	/** Can be 0: scheduled times are whole minutes. */
	public int durationSeconds() {
		return from.departure().secondsUntil(to.arrival());
	}

	/** Share of the leg's time passed at {@code time}, 0 to 1; 1 when the leg takes no time. */
	public double timeFraction(ServiceTime time) {
		int duration = durationSeconds();
		if (duration == 0) {
			return 1.0;
		}
		double fraction = (double) from.departure().secondsUntil(time) / duration;
		return Math.clamp(fraction, 0.0, 1.0);
	}

	/** The point a share {@code distanceFraction} (0..1) of the way along the leg. */
	public GeoPoint pointAt(double distanceFraction) {
		return path.pointAt(distanceFraction * path.lengthM());
	}

	/** No real path: no segments, or a straight-line segment. */
	public boolean isEstimated() {
		return segments.isEmpty() || segments.stream().anyMatch(PathSegment::estimated);
	}

	public boolean mayBeInTunnel() {
		return segments.stream().anyMatch(PathSegment::hasTunnel);
	}

	private static Polyline joined(List<PathSegment> segments) {
		return segments.stream()
				.map(PathSegment::geometry)
				.reduce(Polyline::then)
				.orElseThrow();
	}

	@Override
	public String toString() {
		return "Leg " + from.station().name() + " -> " + to.station().name();
	}
}
