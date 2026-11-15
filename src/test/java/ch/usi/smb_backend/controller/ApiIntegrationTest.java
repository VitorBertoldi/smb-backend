package ch.usi.smb_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.TestcontainersConfiguration;
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
	private static final String IC_712 = "/api/v1/trips/" + DATE + "/85:11:712:001";

	@Autowired
	private MockMvcTester mvc;

	@Test
	void listsTheServiceDays() {
		assertThat(get("/api/v1/days")).hasStatusOk()
				.bodyJson().extractingPath("$.dates").asArray().containsExactly(DATE);
	}

	@Test
	void findsStationsLinesAndPaths() {
		assertThat(get("/api/v1/stations?name=bern")).hasStatusOk()
				.bodyJson().extractingPath("$[0].id").isEqualTo("8507000");
		assertThat(get("/api/v1/stations/8503000")).hasStatusOk()
				.bodyJson().extractingPath("$.level").isEqualTo("UNDERGROUND");
		assertThat(get("/api/v1/lines?name=IC1")).hasStatusOk()
				.bodyJson().extractingPath("$[0].vehicleType").isEqualTo("TRAIN");
		assertThat(get("/api/v1/paths?ids=2,1")).hasStatusOk()
				.bodyJson().extractingPath("$[*].id").asArray().containsExactly(2, 1);
	}

	@Test
	void showsATripWithPlannedAndActualTimes() {
		MvcTestResult trip = get(IC_712);

		assertThat(trip).hasStatusOk().bodyJson().extractingPath("$.displayName").isEqualTo("IC 712");
		assertThat(trip).bodyJson().extractingPath("$.destination.name").isEqualTo("Bern");
		assertThat(trip).bodyJson().extractingPath("$.stopEvents[0].departure.scheduled").isEqualTo("09:32");
		assertThat(trip).bodyJson().extractingPath("$.stopEvents[0].departure.actual").isEqualTo("09:33:10");
		assertThat(trip).bodyJson().extractingPath("$.stopEvents[0].departure.delayS").isEqualTo(70);
		assertThat(trip).bodyJson().extractingPath("$.stopEvents[0].platform").isEqualTo("34");
	}

	@Test
	void placesOneVehicleOnEitherBasis() {
		assertThat(get(IC_712 + "/position?time=09:32:30&basis=scheduled")).hasStatusOk()
				.bodyJson().extractingPath("$.status").isEqualTo("MOVING");
		assertThat(get(IC_712 + "/position?time=09:32:30")).hasStatusOk()
				.bodyJson().extractingPath("$.status").isEqualTo("NOT_RUNNING");
	}

	@Test
	void placesEveryRunningVehicle() {
		MvcTestResult atTen = get("/api/v1/vehicles?date=" + DATE + "&time=10:00&type=train");

		assertThat(atTen).hasStatusOk().bodyJson().extractingPath("$[*].trip.id").asArray()
				.containsExactly("85:11:712:001");
		assertThat(atTen).bodyJson().extractingPath("$[0].previousStation.name").isEqualTo("Zürich HB");
		assertThat(get("/api/v1/vehicles?date=" + DATE + "&time=10:00&type=BUS")).hasStatusOk()
				.bodyJson().extractingPath("$").asArray().isEmpty();
	}

	@Test
	void describesTheMovementsOfAWindow() {
		MvcTestResult movements = get("/api/v1/movements?date=" + DATE + "&from=09:00&to=11:30&basis=scheduled");

		assertThat(movements).hasStatusOk().bodyJson().extractingPath("$[0].pathIds").asArray().containsExactly(1, 2);
		assertThat(movements).bodyJson().extractingPath("$[0].motion").isEqualTo("SMOOTH");
		assertThat(movements).bodyJson().extractingPath("$[0].departS").isEqualTo(34_320);
		assertThat(movements).bodyJson().extractingPath("$[*].trip.id").asArray()
				.as("IC 2512 is planned in this window too").contains("85:11:712:001", "85:11:2512:001");
	}

	@Test
	void showsADepartureBoardWithCancellations() {
		MvcTestResult board = get("/api/v1/stations/8503000/departures?date=" + DATE + "&from=09:00&to=12:00");

		assertThat(board).hasStatusOk().bodyJson().extractingPath("$[*].displayName").asArray()
				.containsExactly("IC 712", "IC 2512");
		assertThat(board).bodyJson().extractingPath("$[1].cancelled").isEqualTo(true);
	}

	@Test
	void servesTheDailyStationStatistics() {
		assertThat(get("/api/v1/stations/stats?date=" + DATE)).hasStatusOk()
				.bodyJson().extractingPath("$[*].station.id").asArray().contains("8503000", "8507000");
	}

	@Test
	void answersUnknownThingsWithNotFound() {
		assertThat(get("/api/v1/stations/0000000")).hasStatus(HttpStatus.NOT_FOUND);
		assertThat(get("/api/v1/trips/" + DATE + "/nope")).hasStatus(HttpStatus.NOT_FOUND);
		assertThat(get("/api/v1/vehicles?date=2026-12-25&time=10:00")).hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson().extractingPath("$.availableDates").asArray().containsExactly(DATE);
	}

	@Test
	void answersUnusableParametersWithBadRequest() {
		assertThat(get("/api/v1/vehicles?date=" + DATE + "&time=9h32")).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson().extractingPath("$.detail").asString().contains("9h32");
		assertThat(get("/api/v1/vehicles?date=21.10.2026&time=10:00")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/vehicles?date=" + DATE + "&time=10:00&basis=maybe")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=11:00&to=10:00")).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/vehicles?date=" + DATE)).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(get("/api/v1/movements?date=" + DATE + "&from=06:00&to=09:01")).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson().extractingPath("$.detail").asString().contains("3 hours");
		assertThat(mvc.get().uri("/api/v1/stations").param("name", " ").exchange()).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void describesItselfWithOpenApi() {
		assertThat(get("/v3/api-docs")).hasStatusOk()
				.bodyJson().extractingPath("$.paths").asMap().containsKeys("/api/v1/movements", "/api/v1/vehicles");
	}

	private MvcTestResult get(String uri) {
		return mvc.get().uri(uri).exchange();
	}
}
