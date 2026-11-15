package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Station;

/** A station named inside another answer. */
public record StationRefDto(String id, String name) {

	public static StationRefDto from(Station station) {
		return new StationRefDto(station.id().code(), station.name());
	}
}
