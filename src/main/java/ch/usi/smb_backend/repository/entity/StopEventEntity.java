package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "stop_events")
public class StopEventEntity {

	@EmbeddedId
	private StopEventEntityId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "service_date", referencedColumnName = "service_date", insertable = false, updatable = false),
			@JoinColumn(name = "trip_id", referencedColumnName = "trip_id", insertable = false, updatable = false)
	})
	private TripEntity trip;

	@Column(name = "station_id")
	private String stationId;

	@Column(name = "platform_sloid")
	private String platformSloid;

	@Column(name = "scheduled_arrival")
	private OffsetDateTime scheduledArrival;

	@Column(name = "scheduled_departure")
	private OffsetDateTime scheduledDeparture;

	@Column(name = "actual_arrival")
	private OffsetDateTime actualArrival;

	@Column(name = "actual_departure")
	private OffsetDateTime actualDeparture;

	@Column(name = "arrival_status")
	private String arrivalStatus;

	@Column(name = "departure_status")
	private String departureStatus;

	@Column(name = "is_stop_cancelled")
	private boolean stopCancelled;

	@Column(name = "is_pass_through")
	private boolean passThrough;

	@Column(name = "next_segment_id")
	private Integer nextSegmentId;

	protected StopEventEntity() {
	}

	public StopEventEntityId getId() {
		return id;
	}

	public String getStationId() {
		return stationId;
	}

	public String getPlatformSloid() {
		return platformSloid;
	}

	public OffsetDateTime getScheduledArrival() {
		return scheduledArrival;
	}

	public OffsetDateTime getScheduledDeparture() {
		return scheduledDeparture;
	}

	public OffsetDateTime getActualArrival() {
		return actualArrival;
	}

	public OffsetDateTime getActualDeparture() {
		return actualDeparture;
	}

	/** Istdaten status: {@code REAL}, {@code GESCHAETZT}, {@code PROGNOSE}, {@code UNBEKANNT} or null. */
	public String getArrivalStatus() {
		return arrivalStatus;
	}

	public String getDepartureStatus() {
		return departureStatus;
	}

	public boolean isStopCancelled() {
		return stopCancelled;
	}

	public boolean isPassThrough() {
		return passThrough;
	}

	public Integer getNextSegmentId() {
		return nextSegmentId;
	}
}
