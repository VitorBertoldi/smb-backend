package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.TripRepository;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.repository.entity.TripEntity;
import ch.usi.smb_backend.repository.entity.TripEntityId;
import ch.usi.smb_backend.repository.entity.TripEntityRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trips with their stop events, loaded per query. A late trip can run after its planned end, so
 * window queries widen the window by {@link #DELAY_MARGIN}; the model then decides exactly.
 */
@Repository
@Transactional(readOnly = true)
class JpaTripRepository implements TripRepository {

	private static final Duration DELAY_MARGIN = Duration.ofHours(1);

	private static final Logger LOG = LoggerFactory.getLogger(JpaTripRepository.class);

	private final TripEntityRepository tripRows;
	private final ReferenceDataCache reference;
	/** A published database never changes, so the dates are read once. */
	private volatile List<LocalDate> serviceDates;

	JpaTripRepository(TripEntityRepository tripRows, ReferenceDataCache reference) {
		this.tripRows = tripRows;
		this.reference = reference;
	}

	@Override
	public List<LocalDate> serviceDates() {
		List<LocalDate> dates = serviceDates;
		if (dates == null) {
			dates = List.copyOf(tripRows.findServiceDates());
			serviceDates = dates;
		}
		return dates;
	}

	@Override
	public Optional<Trip> findByKey(TripKey key) {
		return tripRows.findWithStopEvents(new TripEntityId(key.serviceDate(), key.tripId()))
				.map(row -> TripMapper.toTrip(row, reference.get()));
	}

	@Override
	public List<Trip> findCandidatesRunningBetween(ServiceDay day, ServiceTime from, ServiceTime to) {
		List<TripEntity> rows = tripRows.findOverlapping(
				day.date(),
				timestamp(day, from).minus(DELAY_MARGIN),
				timestamp(day, to).plus(DELAY_MARGIN));
		return validTrips(rows);
	}

	@Override
	public List<Trip> findCandidatesDepartingBetween(StationId station, ServiceDay day, ServiceTime from, ServiceTime to) {
		List<TripEntity> rows = tripRows.findDepartingFrom(
				station.code(), day.date(), timestamp(day, from), timestamp(day, to));
		return validTrips(rows);
	}

	private static OffsetDateTime timestamp(ServiceDay day, ServiceTime time) {
		return day.toInstant(time).atOffset(ZoneOffset.UTC);
	}

	/**
	 * Rows that break a model rule are left out and logged, so one bad trip can't hide a whole
	 * window; they are reported to the data group, never fixed here.
	 */
	private List<Trip> validTrips(List<TripEntity> rows) {
		ReferenceData data = reference.get();
		return rows.stream()
				.flatMap(row -> mapOrSkip(row, data).stream())
				.toList();
	}

	private static Optional<Trip> mapOrSkip(TripEntity row, ReferenceData data) {
		try {
			return Optional.of(TripMapper.toTrip(row, data));
		} catch (IllegalArgumentException invalid) {
			LOG.warn("Skipping an invalid trip in the data: {}", invalid.getMessage());
			return Optional.empty();
		}
	}
}
