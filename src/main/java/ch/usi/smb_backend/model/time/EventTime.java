package ch.usi.smb_backend.model.time;

import java.util.Objects;
import java.util.Optional;

/**
 * One arrival or one departure: what was planned, what happened, and how much to trust the
 * latter. The actual time is missing exactly when the status is {@link TimeStatus#UNKNOWN}.
 */
public record EventTime(ServiceTime scheduled, Optional<ServiceTime> actual, TimeStatus status) {

	public EventTime {
		Objects.requireNonNull(scheduled, "scheduled");
		Objects.requireNonNull(actual, "actual");
		Objects.requireNonNull(status, "status");
		if (actual.isEmpty() != (status == TimeStatus.UNKNOWN)) {
			throw new IllegalArgumentException(
					"an actual time must be given exactly when the status isn't UNKNOWN (status " + status + ")");
		}
	}

	/** Only the plan is known. */
	public static EventTime scheduledOnly(ServiceTime scheduled) {
		return new EventTime(scheduled, Optional.empty(), TimeStatus.UNKNOWN);
	}

	public static EventTime of(ServiceTime scheduled, ServiceTime actual, TimeStatus status) {
		return new EventTime(scheduled, Optional.of(actual), status);
	}

	/** The time on that basis; on {@code ACTUAL} the scheduled time when there's no actual one. */
	public ServiceTime on(TimeBasis basis) {
		return switch (basis) {
			case SCHEDULED -> scheduled;
			case ACTUAL -> actual.orElse(scheduled);
		};
	}

	public Optional<Delay> delay() {
		return actual.map(actualTime -> Delay.between(scheduled, actualTime));
	}

	/** Whether the time on that basis can be trusted: the plan always can, a measurement if reliable. */
	public boolean isReliableOn(TimeBasis basis) {
		return basis == TimeBasis.SCHEDULED || status.isReliable();
	}
}
