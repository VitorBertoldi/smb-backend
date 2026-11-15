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
import ch.usi.smb_backend.model.statistics.StationDayStats;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.EventTime;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.time.TimeStatus;
import ch.usi.smb_backend.model.timetable.StopEvent;
import ch.usi.smb_backend.model.timetable.Trip;
import ch.usi.smb_backend.model.timetable.TripKey;
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
		JpaLineRepository.class,
		JpaPathSegmentRepository.class,
		JpaTripRepository.class,
		JpaStationStatsRepository.class})
class JpaRepositoriesIntegrationTest {

	private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 21);
	private static final ServiceDay DAY = ServiceDay.of(WEDNESDAY);
	private static final TripKey IC_712 = new TripKey(WEDNESDAY, "85:11:712:001");
	private static final StationId ZURICH_HB = new StationId("8503000");
	private static final StationId AARAU = new StationId("8502113");

	@Autowired
	private JpaStationRepository stations;
	@Autowired
	private JpaLineRepository lines;
	@Autowired
	private JpaPathSegmentRepository segments;
	@Autowired
	private JpaTripRepository trips;
	@Autowired
	private JpaStationStatsRepository stats;

	@Test
	void mapsStationsWithTheirLevel() {
		Station zurich = stations.findById(ZURICH_HB).orElseThrow();

		assertThat(zurich.name()).isEqualTo("Zürich HB");
		assertThat(zurich.location()).isEqualTo(new GeoPoint(8.5402, 47.3782, 408));
		assertThat(zurich.level()).isEqualTo(Level.UNDERGROUND);
		assertThat(stations.findById(new StationId("8507000")).orElseThrow().level()).isEqualTo(Level.UNKNOWN);
		assertThat(stations.findByName("zür")).containsExactly(zurich);
	}

	@Test
	void mapsLinesWithTheirOperator() {
		assertThat(lines.findByName("ic1")).singleElement().satisfies(line -> {
			assertThat(line.vehicleType()).isEqualTo(VehicleType.TRAIN);
			assertThat(line.operator().name()).isEqualTo("Schweizerische Bundesbahnen SBB");
		});
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
	void mapsATripWithPlannedAndActualTimes() {
		Trip ic712 = trips.findByKey(IC_712).orElseThrow();
		StopEvent zurich = ic712.stopEvents().getFirst();

		assertThat(ic712.displayName()).isEqualTo("IC 712");
		assertThat(ic712.stopEvents()).hasSize(3);
		assertThat(zurich.arrival()).isEmpty();
		assertThat(zurich.departure()).contains(
				EventTime.of(DAY.parseClock("09:32"), DAY.parseClock("09:33:10"), TimeStatus.MEASURED));
		assertThat(zurich.platform()).contains(new Platform("ch:1:sloid:3000:503:34"));
		assertThat(ic712.stopEvents().get(1).passThrough()).isTrue();
		assertThat(ic712.stopEvents().getLast().arrival().orElseThrow().status()).isEqualTo(TimeStatus.FORECAST);
		assertThat(ic712.timeline(TimeBasis.SCHEDULED).legs()).singleElement()
				.satisfies(leg -> assertThat(leg.segments()).extracting(PathSegment::id).containsExactly(1, 2));
	}

	@Test
	void keepsTimesPastMidnightOnTheirServiceDay() {
		Trip night = trips.findByKey(new TripKey(WEDNESDAY, "85:11:9999:001")).orElseThrow();

		assertThat(DAY.formatClock(night.timeline(TimeBasis.SCHEDULED).lastArrival())).isEqualTo("24:20");
		assertThat(night.extra()).isTrue();
	}

	@Test
	void answersUnknownTripsWithNothing() {
		assertThat(trips.findByKey(new TripKey(WEDNESDAY, "nope"))).isEmpty();
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
	void findsTheTripsDepartingFromAStation() {
		List<Trip> fromZurich = trips.findCandidatesDepartingBetween(
				ZURICH_HB, DAY, DAY.parseClock("09:30"), DAY.parseClock("09:35"));
		List<Trip> fromAarauLate = trips.findCandidatesDepartingBetween(
				AARAU, DAY, DAY.parseClock("23:00"), DAY.parseClock("23:59"));

		assertThat(fromZurich).extracting(Trip::key).containsExactly(IC_712);
		assertThat(fromAarauLate).extracting(Trip::displayName).containsExactly("IC 9999");
	}

	@Test
	void mapsTheDailyStationStatistics() {
		StationDayStats zurich = stats.findByDay(WEDNESDAY).stream()
				.filter(row -> row.station().id().equals(ZURICH_HB))
				.findFirst().orElseThrow();

		assertThat(zurich.events()).isEqualTo(2);
		assertThat(zurich.cancelled()).isEqualTo(1);
		assertThat(zurich.averageDepartureDelay()).contains(Delay.ofSeconds(70));
		assertThat(zurich.onTimeShare().getAsDouble()).isEqualTo(1.0);
	}
}
