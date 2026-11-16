package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.timetable.Trip;
import java.time.LocalDate;
import java.util.List;

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
}
