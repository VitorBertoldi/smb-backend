package ch.usi.smb_backend.model.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class ServiceTimeTest {

	private static final ServiceTime NINE_THIRTY_TWO = ServiceTime.ofSeconds(34_320);
	private static final ServiceTime TWENTY_FIVE_TEN = ServiceTime.ofSeconds(90_600);

	@Test
	void canGoPastMidnightButNotBelowZero() {
		assertThat(TWENTY_FIVE_TEN.seconds()).isGreaterThan(86_400);
		assertThatIllegalArgumentException().isThrownBy(() -> ServiceTime.ofSeconds(-1));
	}

	@Test
	void comparesAndMeasures() {
		assertThat(NINE_THIRTY_TWO.isBefore(TWENTY_FIVE_TEN)).isTrue();
		assertThat(TWENTY_FIVE_TEN.isAfter(NINE_THIRTY_TWO)).isTrue();
		assertThat(NINE_THIRTY_TWO.secondsUntil(TWENTY_FIVE_TEN)).isEqualTo(56_280);
		assertThat(TWENTY_FIVE_TEN.secondsUntil(NINE_THIRTY_TWO)).isEqualTo(-56_280);
		assertThat(NINE_THIRTY_TWO.max(TWENTY_FIVE_TEN)).isEqualTo(TWENTY_FIVE_TEN);
		assertThat(NINE_THIRTY_TWO.plusSeconds(60)).isEqualTo(ServiceTime.ofSeconds(34_380));
	}
}
