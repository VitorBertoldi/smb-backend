package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Departure;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/** A station's departure board for a window, on one time basis. */
@Service
public class DepartureBoardService {

	private final TripRepository trips;
	private final NetworkService network;

	public DepartureBoardService(TripRepository trips, NetworkService network) {
		this.trips = trips;
		this.network = network;
	}

	/**
	 * The departures from the station whose time on that basis lies in {@code [from, to]}, in
	 * that order. Cancelled departures are included and marked.
	 *
	 * @throws NotFoundException when there's no such station
	 */
	public List<Departure> departures(StationId stationId, ServiceDay day, ServiceTime from, ServiceTime to, TimeBasis basis) {
		Station station = network.station(stationId);
		return trips.findCandidatesDepartingBetween(stationId, day, from, to).stream()
				.flatMap(trip -> trip.departures().stream())
				.filter(departure -> departure.station().equals(station))
				.filter(departure -> isWithin(departure.timeOn(basis), from, to))
				.sorted(Comparator.comparing(departure -> departure.timeOn(basis)))
				.toList();
	}

	private static boolean isWithin(ServiceTime time, ServiceTime from, ServiceTime to) {
		return !time.isBefore(from) && !time.isAfter(to);
	}
}
