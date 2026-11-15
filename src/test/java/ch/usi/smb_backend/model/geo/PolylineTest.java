package ch.usi.smb_backend.model.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

class PolylineTest {

	private static final GeoPoint WEST = new GeoPoint(7.0, 47.0, 400);
	private static final GeoPoint MIDDLE = new GeoPoint(7.1, 47.0, 500);
	private static final GeoPoint EAST = new GeoPoint(7.2, 47.0, 600);

	@Test
	void needsAtLeastOnePoint() {
		assertThatIllegalArgumentException().isThrownBy(() -> Polyline.of(List.of()));
	}

	@Test
	void addsUpTheLengthOfEveryStretch() {
		Polyline line = Polyline.of(List.of(WEST, MIDDLE, EAST));

		assertThat(line.lengthM()).isCloseTo(WEST.distanceTo(MIDDLE) + MIDDLE.distanceTo(EAST), within(1e-6));
	}

	@Test
	void interpolatesBetweenTheTwoNearestPoints() {
		Polyline line = Polyline.of(List.of(WEST, MIDDLE, EAST));
		double firstStretchM = WEST.distanceTo(MIDDLE);

		assertThat(line.pointAt(firstStretchM)).isEqualTo(MIDDLE);
		assertThat(line.pointAt(firstStretchM / 2).heightM()).isCloseTo(450, within(1e-6));
		assertThat(line.pointAt(line.lengthM() * 0.75).heightM()).isCloseTo(550, within(0.5));
	}

	@Test
	void clampsToTheEnds() {
		Polyline line = Polyline.straight(WEST, EAST);

		assertThat(line.pointAt(-10)).isEqualTo(WEST);
		assertThat(line.pointAt(line.lengthM() + 10)).isEqualTo(EAST);
	}

	@Test
	void aLineOfLengthZeroAlwaysAnswersItsFirstPoint() {
		Polyline line = Polyline.of(List.of(WEST, WEST));

		assertThat(line.lengthM()).isZero();
		assertThat(line.pointAt(5)).isEqualTo(WEST);
	}

	@Test
	void repeatedPointsDontBreakTheLookup() {
		Polyline line = Polyline.of(List.of(WEST, MIDDLE, MIDDLE, EAST));
		double firstStretchM = WEST.distanceTo(MIDDLE);

		assertThat(line.pointAt(firstStretchM)).isEqualTo(MIDDLE);
		assertThat(line.pointAt(line.lengthM())).isEqualTo(EAST);
	}

	@Test
	void joiningKeepsASharedPointOnce() {
		Polyline joined = Polyline.straight(WEST, MIDDLE).then(Polyline.straight(MIDDLE, EAST));

		assertThat(joined.points()).containsExactly(WEST, MIDDLE, EAST);
	}

	@Test
	void joiningLinesThatDontTouchKeepsEveryPoint() {
		Polyline joined = Polyline.straight(WEST, MIDDLE).then(Polyline.straight(EAST, WEST));

		assertThat(joined.points()).containsExactly(WEST, MIDDLE, EAST, WEST);
	}
}
