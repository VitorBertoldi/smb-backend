package ch.usi.smb_backend.model.network;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_TO_AARAU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ch.usi.smb_backend.model.fixtures.TestNetwork;
import org.junit.jupiter.api.Test;

class NetworkTest {

	@Test
	void aStationIdCantBeBlank() {
		assertThatIllegalArgumentException().isThrownBy(() -> new StationId(" "));
	}

	@Test
	void stationsAreEqualByIdOnly() {
		Station renamed = TestNetwork.station("8503000", "Zurich main station", 0, 0, 0, Level.UNKNOWN);

		assertThat(renamed).isEqualTo(ZURICH_HB).hasSameHashCodeAs(ZURICH_HB);
		assertThat(ZURICH_HB).isNotEqualTo(BERN);
	}

	@Test
	void aPlatformIsLabelledWithTheLastPartOfItsSloid() {
		assertThat(new Platform("ch:1:sloid:3000:503:34").label()).isEqualTo("34");
		assertThat(new Platform("7").label()).isEqualTo("7");
	}

	@Test
	void aPathSegmentLeadsInOneDirectionOnly() {
		assertThat(ZURICH_TO_AARAU.connects(ZURICH_HB, AARAU)).isTrue();
		assertThat(ZURICH_TO_AARAU.connects(AARAU, ZURICH_HB)).isFalse();
		assertThat(ZURICH_TO_AARAU.connects(ZURICH_HB, BERN)).isFalse();
	}
}
