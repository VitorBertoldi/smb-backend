package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.geo.GeoPoint;

public record LocationDto(double longitude, double latitude, double heightM) {

	public static LocationDto from(GeoPoint point) {
		return new LocationDto(point.longitude(), point.latitude(), point.heightM());
	}
}
