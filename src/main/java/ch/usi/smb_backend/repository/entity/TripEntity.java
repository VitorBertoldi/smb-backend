package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "trips")
public class TripEntity {

	@EmbeddedId
	private TripEntityId id;

	@Column(name = "trip_number")
	private String tripNumber;

	@Column(name = "line_id")
	private String lineId;

	@Column(name = "is_cancelled")
	private boolean cancelled;

	@Column(name = "is_extra_trip")
	private boolean extraTrip;

	@Column(name = "first_departure")
	private OffsetDateTime firstDeparture;

	@Column(name = "last_arrival")
	private OffsetDateTime lastArrival;

	@Column(name = "stop_count")
	private int stopCount;

	@OneToMany(mappedBy = "trip")
	@OrderBy("id.stopSequence")
	private List<StopEventEntity> stopEvents;

	protected TripEntity() {
	}

	public TripEntityId getId() {
		return id;
	}

	public String getTripNumber() {
		return tripNumber;
	}

	public String getLineId() {
		return lineId;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isExtraTrip() {
		return extraTrip;
	}

	public OffsetDateTime getFirstDeparture() {
		return firstDeparture;
	}

	public OffsetDateTime getLastArrival() {
		return lastArrival;
	}

	public int getStopCount() {
		return stopCount;
	}

	/** Ordered by stop sequence. */
	public List<StopEventEntity> getStopEvents() {
		return stopEvents;
	}
}
