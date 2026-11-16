package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Trips with all their stop events. The window query returns <em>candidates</em>: every trip
 * that could run in the window, possibly a few more. Callers decide exactly with
 * {@link ch.usi.smb_backend.model.timetable.TripTimeline}.
 */
public interface TripRepository {

	/** Every service date with trips, in order. */
	List<LocalDate> serviceDates();

	/** Trips that may be running at some moment between {@code from} and {@code to}. */
	List<Trip> findCandidatesRunningBetween(ServiceDay day, ServiceTime from, ServiceTime to);

	/** One trip with all its stop events, or empty when there is none (or it is invalid). */
	Optional<Trip> findByKey(TripKey key);

	/** The trips of a service day with that number: several when operators reuse a number. */
	List<Trip> findByNumber(ServiceDay day, String number);
}
