package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public record TripEntityId(
		@Column(name = "service_date") LocalDate serviceDate,
		@Column(name = "trip_id") String tripId) implements Serializable {
}
