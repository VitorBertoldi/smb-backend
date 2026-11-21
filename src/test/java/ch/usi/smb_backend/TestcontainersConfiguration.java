package ch.usi.smb_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * A throw-away PostgreSQL for tests that start Spring: the same major version and schema as the
 * data group's Azure database (mock-db/schema.sql), holding a few rows of 21 Oct 2026. Needs Docker
 * running.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final DockerImageName POSTGRES = DockerImageName.parse("postgres:18");

	/** A pg_dump file: psql runs it while the container initialises, before the sample rows. */
	private static final MountableFile AZURE_SCHEMA = MountableFile.forHostPath("mock-db/schema.sql");

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(POSTGRES)
				.withCopyFileToContainer(AZURE_SCHEMA, "/docker-entrypoint-initdb.d/10-schema.sql")
				.withInitScript("db/sample-data.sql");
	}
}
