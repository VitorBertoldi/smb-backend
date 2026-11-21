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
 * ({@code db/sample-data.sql}): IC 712, a cancelled IC 2512 and an extra night trip.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ApiIntegrationTest {

	private static final String DATE = "2026-10-21";

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
	void servesPathsInTheOrderAsked() {
		assertThat(get("/api/v1/paths?ids=2,1")).hasStatusOk()
				.bodyJson().extractingPath("$[*].id").asArray().containsExactly(2, 1);
	}

	@Test
	void describesTheMovementsOfAWindowAsTheyReallyRan() {
		MvcTestResult movements = get("/api/v1/movements?date=" + DATE + "&from=09:00&to=11:30");

		assertThat(movements).hasStatusOk().bodyJson().extractingPath("$[*].trip.id").asArray()
				.as("IC 2512 is cancelled, so it never moves").containsExactly("85:11:712:001");
		assertThat(movements).bodyJson().extractingPath("$[0].pathIds").asArray().containsExactly(1, 2);
		assertThat(movements).bodyJson().extractingPath("$[0].fromStation.id").isEqualTo("8503000");
		assertThat(movements).bodyJson().extractingPath("$[0].toStation.id").isEqualTo("8507000");
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
						"/api/v1/days", "/api/v1/stations", "/api/v1/movements", "/api/v1/paths");
	}

	private MvcTestResult get(String uri) {
		return mvc.get().uri(uri).exchange();
	}
}
