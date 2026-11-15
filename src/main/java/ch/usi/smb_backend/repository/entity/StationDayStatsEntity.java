package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** One row of the data group's {@code station_day_stats} view. */
@Entity
@Immutable
@Table(name = "station_day_stats")
public class StationDayStatsEntity {

	@EmbeddedId
	private StationDayStatsEntityId id;

	@Column(name = "n_events")
	private int events;

	@Column(name = "n_cancelled")
	private int cancelled;

	@Column(name = "avg_dep_delay_s")
	private Integer averageDepartureDelaySeconds;

	@Column(name = "pct_on_time")
	private Float percentOnTime;

	protected StationDayStatsEntity() {
	}

	public StationDayStatsEntityId getId() {
		return id;
	}

	public int getEvents() {
		return events;
	}

	public int getCancelled() {
		return cancelled;
	}

	/** Null when nothing departed with a measured time. */
	public Integer getAverageDepartureDelaySeconds() {
		return averageDepartureDelaySeconds;
	}

	/** 0 to 100; null when nothing departed with a measured time. */
	public Float getPercentOnTime() {
		return percentOnTime;
	}
}
