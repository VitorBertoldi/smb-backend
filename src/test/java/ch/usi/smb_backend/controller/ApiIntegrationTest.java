package ch.usi.smb_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.TestcontainersConfiguration;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * The whole backend, from HTTP to PostgreSQL, on the sample rows of 21 Oct 2026
 * ({@code db/gold-sample-data.sql}): IC 712, a cancelled IC 2512 and an extra night trip.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ApiIntegrationTest {

	private static final String DATE = "2026-10-21";
	private static final String IC_712 = "85:11:712:001";

	@Autowired
	private MockMvcTester mvc;

	@Test
	void listsTheServiceDays() {
		assertThat(get("/api/v1/days")).hasStatusOk()
				.bodyJson().extractingPath("$.dates").asArray().containsExactly(DATE);
	}

	@Test
	void listsEveryStationWithItsLocation() {
		MvcTestResult stations = get("/api/v1/stations");

		assertThat(stations).hasStatusOk().bodyJson().extractingPath("$[*].id").asArray()
				.containsExactly("8502113", "8503000", "8507000");
		assertThat(stations).bodyJson().extractingPath("$[1].name").isEqualTo("Zürich HB");
		assertThat(stations).bodyJson().extractingPath("$[1].location.longitude").isEqualTo(8.5402);
		assertThat(stations).bodyJson().extractingPath("$[1].location.latitude").isEqualTo(47.3782);
	}

	@Test
	void servesOneStationById() {
		assertThat(get("/api/v1/stations/8503000")).hasStatusOk().bodyJson()
				.extractingPath("$.name").isEqualTo("Zürich HB");
		assertThat(get("/api/v1/stations/0000000")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void servesOnePathById() {
		MvcTestResult path = get("/api/v1/paths/1");

		assertThat(path).hasStatusOk().bodyJson().extractingPath("$.coordinates").asArray().hasSize(2);
		assertThat(path).bodyJson().extractingPath("$.toStation").isEqualTo("8502113");
		assertThat(get("/api/v1/paths/99")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void refusesTooManyPathIdsAtOnce() {
		String ids = IntStream.rangeClosed(1, 201).mapToObj(String::valueOf).collect(Collectors.joining(","));

		assertThat(get("/api/v1/paths?ids=" + ids)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void servesOneTripWithItsStopsAsIds() {
		MvcTestResult trip = get("/api/v1/trips/" + DATE + "/" + IC_712);

		assertThat(trip).hasStatusOk().bodyJson().extractingPath("$.name").isEqualTo("IC 712");
		assertThat(trip).bodyJson().extractingPath("$.stops[*].stationId").asArray()
				.containsExactly("8503000", "8502113", "8507000");
		assertThat(trip).bodyJson().extractingPath("$.stops[0].scheduledDepartS").isEqualTo(34_320);
		assertThat(trip).bodyJson().extractingPath("$.stops[0].actualDepartS").isEqualTo(34_390);
		assertThat(trip).bodyJson().extractingPath("$.stops[0].scheduledArriveS").isNull();
		assertThat(trip).bodyJson().extractingPath("$.stops[1].passThrough").isEqualTo(true);
		assertThat(get("/api/v1/trips/" + DATE + "/nope")).hasStatus(HttpStatus.NOT_FOUND);
		assertThat(get("/api/v1/trips/2026-12-25/" + IC_712)).hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson().extractingPath("$.availableDates").asArray().containsExactly(DATE);
	}

	@Test
	void servesTheLegsOfOneTrip() {
		assertThat(get("/api/v1/trips/" + DATE + "/" + IC_712 + "/movements")).hasStatusOk().bodyJson()
				.extractingPath("$[0].toStationId").isEqualTo("8507000");
		assertThat(get("/api/v1/trips/" + DATE + "/85:11:2512:001/movements")).hasStatusOk().bodyJson()
				.extractingPath("$").asArray().as("cancelled trips never move").isEmpty();
	}

	@Test
	void findsATripByItsNumber() {
		assertThat(get("/api/v1/trips?date=" + DATE + "&number=712")).hasStatusOk().bodyJson()
				.extractingPath("$[*].id").asArray().containsExactly(IC_712);
		assertThat(get("/api/v1/trips?date=" + DATE + "&number=1")).hasStatusOk().bodyJson()
				.extractingPath("$").asArray().isEmpty();
		assertThat(get("/api/v1/trips?date=" + DATE + "&number=")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void servesPathsInTheOrderAsked() {
		assertThat(get("/api/v1/paths?ids=2,1")).hasStatusOk()
				.bodyJson().extractingPath("$[*].id").asArray().containsExactly(2, 1);
	}

	@Test
	void describesTheMovementsOfAWindowAsTheyReallyRan() {
		MvcTestResult movements = get("/api/v1/movements?date=" + DATE + "&from=09:00&to=11:30");

		assertThat(movements).hasStatusOk().bodyJson().extractingPath("$[*].tripId").asArray()
				.as("IC 2512 is cancelled, so it never moves").containsExactly("85:11:712:001");
		assertThat(movements).bodyJson().extractingPath("$[0].pathIds").asArray().containsExactly(1, 2);
		assertThat(movements).bodyJson().extractingPath("$[0].fromStationId").isEqualTo("8503000");
		assertThat(movements).bodyJson().extractingPath("$[0].toStationId").isEqualTo("8507000");
		assertThat(movements).bodyJson().extractingPath("$[0].departS").isEqualTo(34_390);
		assertThat(movements).bodyJson().extractingPath("$[0].arriveS").isEqualTo(37_840);
		assertThat(movements).bodyJson().extractingPath("$[0].motion").isEqualTo("SMOOTH");
	}

	@Test
	void answersAnUnknownDayWithTheDaysThereAre() {
		assertThat(get("/api/v1/movements?date=2026-12-25&from=10:00&to=11:00")).hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson().extractingPath("$.availableDates").asArray().containsExactly(DATE);
	}

	@Test
	void answersUnusableParametersWithBadRequest() {
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=9h32&to=10:00")).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson().extractingPath("$.detail").asString().contains("9h32");
		assertThat(get("/api/v1/movements?date=21.10.2026&from=09:00&to=10:00")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=09:00")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=11:00&to=10:00")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=06:00&to=09:01")).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson().extractingPath("$.detail").asString().contains("3 hours");
	}

	@Test
	void describesItselfWithOpenApi() {
		assertThat(get("/v3/api-docs")).hasStatusOk()
				.bodyJson().extractingPath("$.paths").asMap().containsOnlyKeys(
						"/api/v1/days", "/api/v1/stations", "/api/v1/stations/{id}", "/api/v1/movements",
						"/api/v1/paths", "/api/v1/paths/{id}", "/api/v1/trips", "/api/v1/trips/{date}/{tripId}",
						"/api/v1/trips/{date}/{tripId}/movements");
	}

	private MvcTestResult get(String uri) {
		return mvc.get().uri(uri).exchange();
	}
}
