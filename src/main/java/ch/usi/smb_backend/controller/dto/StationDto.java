package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Station;

public record StationDto(String id, String name, LocationDto location) {

	public static StationDto from(Station station) {
		return new StationDto(station.id().code(), station.name(), LocationDto.from(station.location()));
	}
}
