package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.geo.Polyline;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.NetworkType;
import ch.usi.smb_backend.model.network.Operator;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.repository.entity.LineEntity;
import ch.usi.smb_backend.repository.entity.PathSegmentEntity;
import ch.usi.smb_backend.repository.entity.StationEntity;
import java.util.List;
import java.util.Map;

/** Turns reference rows (stations, lines, path segments) into model objects. */
final class ReferenceDataMapper {

	/** Phase 1 paths may come without heights; the map then draws them at sea level. */
	private static final double UNKNOWN_HEIGHT_M = 0;
	private static final int LONGITUDE = 0;
	private static final int LATITUDE = 1;
	private static final int HEIGHT = 2;
	private static final int MINIMUM_PATH_POINTS = 2;

	private ReferenceDataMapper() {
	}

	static Station toStation(StationEntity row) {
		double heightM = row.getElevation() == null ? UNKNOWN_HEIGHT_M : row.getElevation();
		return new Station(
				new StationId(row.getStationId()),
				row.getSloid(),
				row.getName(),
				new GeoPoint(row.getLongitude(), row.getLatitude(), heightM),
				// stations.is_underground (from OSM) is not in the data group's database yet; read it
				// again (and map true/false/null to UNDERGROUND/SURFACE/UNKNOWN) once they add it.
				Level.UNKNOWN);
	}

	static Line toLine(LineEntity row) {
		return new Line(
				row.getLineId(),
				row.getName(),
				row.getCategory(),
				enumValue(VehicleType.class, row.getVehicleType(), "lines.vehicle_type"),
				new Operator(row.getAgencyCode(), row.getAgencyName()));
	}

	static PathSegment toSegment(PathSegmentEntity row, Map<StationId, Station> stations) {
		Station from = requiredStation(stations, row.getFromStationId(), row);
		Station to = requiredStation(stations, row.getToStationId(), row);
		return new PathSegment(
				row.getSegmentId(),
				from,
				to,
				enumValue(NetworkType.class, row.getNetwork(), "path_segments.network"),
				toPolyline(row.getPoints(), from, to),
				row.isEstimated(),
				Boolean.TRUE.equals(row.getHasTunnel()));
	}

	/** A path with fewer than two points becomes the straight line between its stations. */
	private static Polyline toPolyline(List<List<Double>> points, Station from, Station to) {
		if (points == null || points.size() < MINIMUM_PATH_POINTS) {
			return Polyline.straight(from.location(), to.location());
		}
		return Polyline.of(points.stream().map(ReferenceDataMapper::toGeoPoint).toList());
	}

	private static GeoPoint toGeoPoint(List<Double> coordinates) {
		double heightM = coordinates.size() > HEIGHT ? coordinates.get(HEIGHT) : UNKNOWN_HEIGHT_M;
		return new GeoPoint(coordinates.get(LONGITUDE), coordinates.get(LATITUDE), heightM);
	}

	private static Station requiredStation(Map<StationId, Station> stations, String id, PathSegmentEntity row) {
		Station station = stations.get(new StationId(id));
		if (station == null) {
			throw new IllegalStateException("path segment " + row.getSegmentId() + " refers to unknown station " + id);
		}
		return station;
	}

	/** An unknown value stops the start-up with a clear message, so it's noticed at once. */
	private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String column) {
		try {
			return Enum.valueOf(type, value);
		} catch (IllegalArgumentException | NullPointerException unknown) {
			throw new IllegalStateException(column + " has a value the backend doesn't know: '" + value + "'", unknown);
		}
	}
}
