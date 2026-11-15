package ch.usi.smb_backend.model.timetable;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_TO_AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestNetwork;
import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.List;
import org.junit.jupiter.api.Test;

class LegTest {

	@Test
	void measuresTheShareOfTimePassed() {
		Leg leg = between(ZURICH_HB, "10:00", AARAU, "10:30", List.of(ZURICH_TO_AARAU));

		assertThat(leg.timeFraction(at("10:15"))).isEqualTo(0.5);
		assertThat(leg.timeFraction(at("09:00"))).isZero();
		assertThat(leg.timeFraction(at("11:00"))).isEqualTo(1.0);
	}

	@Test
	void aLegThatTakesNoTimeIsAlreadyOver() {
		Leg instant = between(ZURICH_HB, "10:00", AARAU, "10:00", List.of(ZURICH_TO_AARAU));

		assertThat(instant.durationSeconds()).isZero();
		assertThat(instant.timeFraction(at("10:00"))).isEqualTo(1.0);
		assertThat(instant.isMovingAt(at("10:00"))).isFalse();
	}

	@Test
	void followsItsSegmentsJoined() {
		Leg ic712 = TestTrips.ic712().timeline(TimeBasis.SCHEDULED).legs().getFirst();

		assertThat(ic712.path().points()).containsExactly(ZURICH_HB.location(), AARAU.location(), BERN.location());
		assertThat(ic712.pointAt(0)).isEqualTo(ZURICH_HB.location());
		assertThat(ic712.pointAt(1)).isEqualTo(BERN.location());
	}

	@Test
	void withoutAPathIsAStraightLineAndEstimated() {
		Leg noPath = between(ZURICH_HB, "10:00", BERN, "11:00", List.of());

		assertThat(noPath.isEstimated()).isTrue();
		assertThat(noPath.path().points()).containsExactly(ZURICH_HB.location(), BERN.location());
		assertThat(noPath.pointAt(0.5).latitude())
				.isCloseTo((ZURICH_HB.location().latitude() + BERN.location().latitude()) / 2, within(1e-9));
	}

	@Test
	void mayBeInATunnelWhenASegmentHasOne() {
		Leg tunnel = between(ZURICH_HB, "10:00", AARAU, "10:30", List.of(TestNetwork.trackWithTunnel(9, ZURICH_HB, AARAU)));

		assertThat(tunnel.mayBeInTunnel()).isTrue();
		assertThat(tunnel.isEstimated()).isFalse();
	}

	private static Leg between(Station from, String departs, Station to, String arrives, List<PathSegment> segments) {
		StopEvent start = StopEventBuilder.at(from).departs(departs).buildAlone();
		StopEvent end = StopEventBuilder.at(to).arrives(arrives).buildAlone();
		Stop fromStop = new Stop(start, at(departs), at(departs), TimeBasis.SCHEDULED);
		Stop toStop = new Stop(end, at(arrives), at(arrives), TimeBasis.SCHEDULED);
		return new Leg(fromStop, toStop, segments);
	}
}
