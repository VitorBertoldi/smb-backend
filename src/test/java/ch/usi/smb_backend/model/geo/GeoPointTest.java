package ch.usi.smb_backend.model.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoPointTest {

	private static final GeoPoint ZURICH_HB = new GeoPoint(8.5402, 47.3782, 408);
	private static final GeoPoint BERN = new GeoPoint(7.4391, 46.9490, 540);

	@Test
	void rejectsCoordinatesOutsideTheEarth() {
		assertThatIllegalArgumentException().isThrownBy(() -> new GeoPoint(181, 0, 0));
		assertThatIllegalArgumentException().isThrownBy(() -> new GeoPoint(0, -91, 0));
		assertThatIllegalArgumentException().isThrownBy(() -> new GeoPoint(Double.NaN, 0, 0));
		assertThatIllegalArgumentException().isThrownBy(() -> new GeoPoint(0, 0, Double.POSITIVE_INFINITY));
	}

	@Test
	void measuresTheDistanceAlongTheGround() {
		// Zürich HB to Bern as the crow flies is about 95 km.
		assertThat(ZURICH_HB.distanceTo(BERN)).isCloseTo(95_500, within(1_000.0));
		assertThat(ZURICH_HB.distanceTo(ZURICH_HB)).isZero();
	}

	@Test
	void findsThePointPartWayToAnother() {
		GeoPoint halfWay = ZURICH_HB.towards(BERN, 0.5);

		assertThat(halfWay.longitude()).isCloseTo((8.5402 + 7.4391) / 2, within(1e-9));
		assertThat(halfWay.latitude()).isCloseTo((47.3782 + 46.9490) / 2, within(1e-9));
		assertThat(halfWay.heightM()).isCloseTo(474, within(1e-9));
	}
}
