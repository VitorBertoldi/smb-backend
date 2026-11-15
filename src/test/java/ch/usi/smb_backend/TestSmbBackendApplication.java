package ch.usi.smb_backend;

import org.springframework.boot.SpringApplication;

/**
 * Runs the backend on a throw-away PostgreSQL with the test schema and sample rows, so the API
 * can be tried without the data group's image: {@code ./mvnw spring-boot:test-run}, then open
 * http://localhost:8080/swagger-ui.html. Needs Docker running; the data is gone when it stops.
 */
public class TestSmbBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmbBackendApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}
}
