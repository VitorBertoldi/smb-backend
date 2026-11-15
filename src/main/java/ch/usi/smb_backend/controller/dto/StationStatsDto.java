package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.statistics.StationDayStats;
import ch.usi.smb_backend.model.time.Delay;

public record StationStatsDto(
		StationRefDto station,
		LocationDto location,
		int events,
		int cancelled,
		Integer averageDepartureDelayS,
		Double onTimeShare) {

	public static StationStatsDto from(StationDayStats stats) {
		return new StationStatsDto(
				StationRefDto.from(stats.station()),
				LocationDto.from(stats.station().location()),
				stats.events(),
				stats.cancelled(),
				stats.averageDepartureDelay().map(Delay::seconds).orElse(null),
				stats.onTimeShare().isPresent() ? stats.onTimeShare().getAsDouble() : null);
	}
}
