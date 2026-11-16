package ch.usi.smb_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.TestcontainersConfiguration;
import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.NetworkType;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Platform;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.time.TimeStatus;
import ch.usi.smb_backend.model.timetable.StopEvent;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * The JPA layer against a real PostgreSQL holding the gold schema and a few rows
 * ({@code db/gold-schema.sql}, {@code db/gold-sample-data.sql}). Starting the context also proves
 * that every entity matches the schema ({@code ddl-auto=validate}).
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
		TestcontainersConfiguration.class,
		ReferenceDataCache.class,
		JpaStationRepository.class,
		JpaPathSegmentRepository.class,
		JpaTripRepository.class})
class JpaRepositoriesIntegrationTest {

	private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 21);
	private static final ServiceDay DAY = ServiceDay.of(WEDNESDAY);
	private static final TripKey IC_712 = new TripKey(WEDNESDAY, "85:11:712:001");

	@Autowired
	private JpaStationRepository stations;
	@Autowired
	private JpaPathSegmentRepository segments;
	@Autowired
	private JpaTripRepository trips;
	@Autowired
	private EntityManager entityManager;

	@Test
	void listsEveryStationByIdWithItsLocationAndLevel() {
		List<Station> all = stations.findAll();

		assertThat(all).extracting(Station::name).containsExactly("Aarau", "Zürich HB", "Bern");
		Station zurich = all.get(1);
		assertThat(zurich.location()).isEqualTo(new GeoPoint(8.5402, 47.3782, 408));
		assertThat(zurich.level()).isEqualTo(Level.UNDERGROUND);
		assertThat(all.getLast().level()).isEqualTo(Level.UNKNOWN);
	}

	@Test
	void findsOneStationAndOnePathById() {
		assertThat(stations.findById(new StationId("8503000"))).get().extracting(Station::name).isEqualTo("Zürich HB");
		assertThat(stations.findById(new StationId("0000000"))).isEmpty();
		assertThat(segments.findById(1)).get().extracting(PathSegment::id).isEqualTo(1);
		assertThat(segments.findById(99)).isEmpty();
	}

	@Test
	void mapsPathSegmentsInTheOrderAsked() {
		List<PathSegment> found = segments.findByIds(List.of(2, 99, 1));

		assertThat(found).extracting(PathSegment::id).containsExactly(2, 1);
		assertThat(found.getFirst().network()).isEqualTo(NetworkType.TRACK);
		assertThat(found.getFirst().geometry().points()).hasSize(2);
		assertThat(found.getFirst().estimated()).isTrue();
	}

	@Test
	void listsTheServiceDates() {
		assertThat(trips.serviceDates()).containsExactly(WEDNESDAY);
	}

	@Test
	void findsTheTripsThatMayRunInAWindow() {
		List<Trip> atTen = trips.findCandidatesRunningBetween(DAY, DAY.parseClock("10:00"), DAY.parseClock("10:05"));
		List<Trip> atThree = trips.findCandidatesRunningBetween(DAY, DAY.parseClock("15:00"), DAY.parseClock("15:05"));

		assertThat(atTen).extracting(Trip::key).contains(IC_712);
		assertThat(atThree).isEmpty();
	}

	@Test
	void mapsATripWithItsLinePlannedAndActualTimes() {
		Trip ic712 = tripAround("10:00", IC_712);
		StopEvent zurich = ic712.stopEvents().getFirst();

		assertThat(ic712.displayName()).isEqualTo("IC 712");
		assertThat(ic712.line().vehicleType()).isEqualTo(VehicleType.TRAIN);
		assertThat(ic712.line().operator().name()).isEqualTo("Schweizerische Bundesbahnen SBB");
		assertThat(ic712.stopEvents()).hasSize(3);
		assertThat(zurich.arrival()).isEmpty();
		assertThat(zurich.departure()).contains(
				EventTime.of(DAY.parseClock("09:32"), DAY.parseClock("09:33:10"), TimeStatus.MEASURED));
		assertThat(zurich.platform()).contains(new Platform("ch:1:sloid:3000:503:34"));
		assertThat(ic712.stopEvents().get(1).passThrough()).isTrue();
		assertThat(ic712.stopEvents().getLast().arrival().orElseThrow().status()).isEqualTo(TimeStatus.FORECAST);
		assertThat(ic712.timeline(TimeBasis.ACTUAL).legs()).singleElement()
				.satisfies(leg -> assertThat(leg.segments()).extracting(PathSegment::id).containsExactly(1, 2));
	}

	@Test
	void keepsTimesPastMidnightOnTheirServiceDay() {
		Trip night = tripAround("24:00", new TripKey(WEDNESDAY, "85:11:9999:001"));

		assertThat(DAY.formatClock(night.timeline(TimeBasis.SCHEDULED).lastArrival())).isEqualTo("24:20");
		assertThat(night.extra()).isTrue();
	}

	@Test
	void findsOneTripByKeyWithAllItsStopEvents() {
		assertThat(trips.findByKey(IC_712)).get().satisfies(trip -> assertThat(trip.stopEvents()).hasSize(3));
		assertThat(trips.findByKey(new TripKey(WEDNESDAY, "nope"))).isEmpty();
	}

	@Test
	void findsTheTripsOfADayByNumber() {
		assertThat(trips.findByNumber(DAY, "712")).extracting(Trip::key).containsExactly(IC_712);
		assertThat(trips.findByNumber(DAY, "1")).isEmpty();
	}

	@Test
	void leavesOutATripThatPointsAtAnUnknownStationInsteadOfFailing() {
		// The connection is read-only and the foreign keys would refuse such a row, so both are
		// switched off for this (rolled back) transaction.
		entityManager.createNativeQuery("SET LOCAL transaction_read_only = off").executeUpdate();
		entityManager.createNativeQuery("SET LOCAL session_replication_role = replica").executeUpdate();
		entityManager.createNativeQuery("""
				INSERT INTO transport.trips VALUES ('2026-10-21', 'broken', '1', '11:IC1', FALSE, FALSE,
				    '2026-10-21 10:00:00+02', '2026-10-21 10:30:00+02', 2)""").executeUpdate();
		entityManager.createNativeQuery("""
				INSERT INTO transport.stop_events (service_date, trip_id, stop_sequence, station_id,
				    scheduled_arrival, scheduled_departure, is_stop_cancelled, is_pass_through) VALUES
				    ('2026-10-21', 'broken', 1, '9999999', NULL, '2026-10-21 10:00:00+02', FALSE, FALSE),
				    ('2026-10-21', 'broken', 2, '8507000', '2026-10-21 10:30:00+02', NULL, FALSE, FALSE)""")
				.executeUpdate();

		assertThat(trips.findCandidatesRunningBetween(DAY, DAY.parseClock("10:00"), DAY.parseClock("10:05")))
				.extracting(Trip::key).contains(IC_712).doesNotContain(new TripKey(WEDNESDAY, "broken"));
		assertThat(trips.findByKey(new TripKey(WEDNESDAY, "broken"))).isEmpty();
		assertThat(trips.findByNumber(DAY, "1")).isEmpty();
	}

	private Trip tripAround(String clock, TripKey key) {
		return trips.findCandidatesRunningBetween(DAY, DAY.parseClock(clock), DAY.parseClock(clock)).stream()
				.filter(trip -> trip.key().equals(key))
				.findFirst().orElseThrow();
	}
}
