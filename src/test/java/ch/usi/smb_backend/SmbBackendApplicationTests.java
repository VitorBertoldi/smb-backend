package ch.usi.smb_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SmbBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
