package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Stations, lines and path segments: small, shared by every trip, and kept in memory. A trip row
 * points into this data by id; an id that isn't here means the database lost its integrity.
 */
record ReferenceData(
		Map<StationId, Station> stations,
		Map<String, Line> lines,
		Map<Integer, PathSegment> segments) {

	ReferenceData {
		stations = Map.copyOf(stations);
		lines = Map.copyOf(lines);
		segments = Map.copyOf(segments);
	}

	Optional<Station> findStation(StationId id) {
		return Optional.ofNullable(stations.get(id));
	}

	Optional<PathSegment> findSegment(int id) {
		return Optional.ofNullable(segments.get(id));
	}

	Station station(StationId id) {
		return required(stations.get(id), "station", id);
	}

	Line line(String id) {
		return required(lines.get(id), "line", id);
	}

	PathSegment segment(int id) {
		return required(segments.get(id), "path segment", id);
	}

	Collection<Station> allStations() {
		return stations.values();
	}

	Collection<Line> allLines() {
		return lines.values();
	}

	private static <T> T required(T value, String kind, Object id) {
		if (value == null) {
			throw new IllegalStateException("the database refers to unknown " + kind + " " + id);
		}
		return value;
	}
}
