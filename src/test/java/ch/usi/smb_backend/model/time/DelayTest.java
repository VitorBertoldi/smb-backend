package ch.usi.smb_backend.model.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DelayTest {

	@Test
	void isActualMinusScheduled() {
		Delay early = Delay.between(ServiceTime.ofSeconds(1_000), ServiceTime.ofSeconds(970));

		assertThat(early.seconds()).isEqualTo(-30);
	}

	@Test
	void isOnTimeBelowThreeMinutes() {
		assertThat(Delay.ofSeconds(179).isOnTime()).isTrue();
		assertThat(Delay.ofSeconds(180).isOnTime()).isFalse();
		assertThat(Delay.ofSeconds(-120).isOnTime()).isTrue();
	}

	@Test
	void printsMinutesAndSecondsWithASign() {
		assertThat(Delay.ofSeconds(70)).hasToString("+1:10");
		assertThat(Delay.ofSeconds(-30)).hasToString("-0:30");
		assertThat(Delay.ofSeconds(0)).hasToString("+0:00");
	}
}
