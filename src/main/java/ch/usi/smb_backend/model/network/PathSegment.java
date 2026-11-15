package ch.usi.smb_backend.model.network;

import ch.usi.smb_backend.model.geo.Polyline;
import java.util.Objects;

/**
 * The path from one station to the next, in that direction, shared by every trip that makes the
 * hop. The opposite direction is a separate segment. Equal by id.
 */
public final class PathSegment {

	private final int id;
	private final Station from;
	private final Station to;
	private final NetworkType network;
	private final Polyline geometry;
	private final boolean estimated;
	private final boolean hasTunnel;

	public PathSegment(
			int id,
			Station from,
			Station to,
			NetworkType network,
			Polyline geometry,
			boolean estimated,
			boolean hasTunnel) {
		this.id = id;
		this.from = Objects.requireNonNull(from, "from");
		this.to = Objects.requireNonNull(to, "to");
		this.network = Objects.requireNonNull(network, "network");
		this.geometry = Objects.requireNonNull(geometry, "geometry");
		this.estimated = estimated;
		this.hasTunnel = hasTunnel;
	}

	public int id() {
		return id;
	}

	public Station from() {
		return from;
	}

	public Station to() {
		return to;
	}

	public NetworkType network() {
		return network;
	}

	public Polyline geometry() {
		return geometry;
	}

	/** True for a straight line between the stations: no real route was found. */
	public boolean estimated() {
		return estimated;
	}

	/** Somewhere along the segment there is a tunnel; where exactly isn't known. */
	public boolean hasTunnel() {
		return hasTunnel;
	}

	/** Whether this segment leads from {@code start} to {@code end} (one direction only). */
	public boolean connects(Station start, Station end) {
		return from.equals(start) && to.equals(end);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof PathSegment segment && id == segment.id;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(id);
	}

	@Override
	public String toString() {
		return "PathSegment " + id + ": " + from.name() + " -> " + to.name();
	}
}
