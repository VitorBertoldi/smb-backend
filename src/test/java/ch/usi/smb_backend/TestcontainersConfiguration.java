package ch.usi.smb_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A throw-away PostgreSQL for tests that start Spring: the same major version as the data group's
 * image, holding the gold schema and a few rows of 21 Oct 2026. Needs Docker running.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final DockerImageName POSTGRES = DockerImageName.parse("postgres:17");

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(POSTGRES)
				.withInitScripts("db/gold-schema.sql", "db/gold-sample-data.sql");
	}
}
