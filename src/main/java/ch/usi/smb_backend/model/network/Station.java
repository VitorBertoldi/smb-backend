package ch.usi.smb_backend.model.network;

import ch.usi.smb_backend.model.geo.GeoPoint;
import java.util.Objects;

/** A place where you can board. Platforms are grouped into their station. Equal by id. */
public final class Station {

	private final StationId id;
	private final String sloid;
	private final String name;
	private final GeoPoint location;
	private final Level level;

	public Station(StationId id, String sloid, String name, GeoPoint location, Level level) {
		this.id = Objects.requireNonNull(id, "id");
		this.sloid = Objects.requireNonNull(sloid, "sloid");
		this.name = Objects.requireNonNull(name, "name");
		this.location = Objects.requireNonNull(location, "location");
		this.level = Objects.requireNonNull(level, "level");
	}

	public StationId id() {
		return id;
	}

	public String sloid() {
		return sloid;
	}

	public String name() {
		return name;
	}

	public GeoPoint location() {
		return location;
	}

	public Level level() {
		return level;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Station station && id.equals(station.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return name + " (" + id + ")";
	}
}
