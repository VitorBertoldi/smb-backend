package ch.usi.smb_backend.model.timetable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Identifies a trip. The Istdaten trip id repeats every operating day, so it is only unique
 * together with its service date.
 */
public record TripKey(LocalDate serviceDate, String tripId) {

	public TripKey {
		Objects.requireNonNull(serviceDate, "serviceDate");
		if (tripId == null || tripId.isBlank()) {
			throw new IllegalArgumentException("a trip id can't be blank");
		}
	}

	@Override
	public String toString() {
		return serviceDate + "/" + tripId;
	}
}
