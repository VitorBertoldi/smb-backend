package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public record StationDayStatsEntityId(
		@Column(name = "station_id") String stationId,
		@Column(name = "service_date") LocalDate serviceDate) implements Serializable {
}
