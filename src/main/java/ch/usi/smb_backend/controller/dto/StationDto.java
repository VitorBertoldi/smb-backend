package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.Station;

public record StationDto(String id, String sloid, String name, LocationDto location, Level level) {

	public static StationDto from(Station station) {
		return new StationDto(station.id().code(), station.sloid(), station.name(),
				LocationDto.from(station.location()), station.level());
	}
}
