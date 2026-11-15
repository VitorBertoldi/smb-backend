package ch.usi.smb_backend.model.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MotionProfileTest {

	@ParameterizedTest
	@EnumSource(MotionProfile.class)
	void startsAtTheStartAndEndsAtTheEnd(MotionProfile profile) {
		assertThat(profile.apply(0)).isZero();
		assertThat(profile.apply(1)).isEqualTo(1.0);
		assertThat(profile.apply(0.5)).isEqualTo(0.5);
	}

	@Test
	void smoothIsSlowAtTheEndsAndLinearIsNot() {
		assertThat(MotionProfile.SMOOTH.apply(0.1)).isLessThan(0.1);
		assertThat(MotionProfile.SMOOTH.apply(0.9)).isGreaterThan(0.9);
		assertThat(MotionProfile.LINEAR.apply(0.1)).isEqualTo(0.1);
	}
}
