package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.network.NetworkType;
import ch.usi.smb_backend.model.network.PathSegment;
import java.util.List;

/** A path segment's geometry, as {@code [[longitude, latitude, heightM], ...]} in travel order. */
public record PathDto(
		int id,
		String fromStation,
		String toStation,
		NetworkType network,
		boolean estimated,
		boolean hasTunnel,
		List<List<Double>> coordinates) {

	public static PathDto from(PathSegment segment) {
		return new PathDto(
				segment.id(),
				segment.from().id().code(),
				segment.to().id().code(),
				segment.network(),
				segment.estimated(),
				segment.hasTunnel(),
				segment.geometry().points().stream().map(PathDto::coordinates).toList());
	}

	private static List<Double> coordinates(GeoPoint point) {
		return List.of(point.longitude(), point.latitude(), point.heightM());
	}
}
