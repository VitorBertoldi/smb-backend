package ch.usi.smb_backend.model.vehicle;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.Leg;
import ch.usi.smb_backend.model.timetable.Trip;
import org.junit.jupiter.api.Test;

/** The IC 712 table of the domain model, section 10. */
class VehiclePositionTest {

	private final Trip ic712 = TestTrips.ic712();
	private final Vehicle train = new VehicleFactory().create(ic712);

	@Test
	void thePlanSaysItHasLeft() {
		VehiclePosition position = train.positionAt(at("09:32:30"), TimeBasis.SCHEDULED);

		assertThat(position.status()).isEqualTo(VehicleStatus.MOVING);
		assertThat(position.previousStation()).contains(ZURICH_HB);
		assertThat(position.nextStation()).as("Aarau is passed, so the next stop is Bern").contains(BERN);
	}

	@Test
	void inRealityItHasNotLeftYet() {
		VehiclePosition position = train.positionAt(at("09:32:30"), TimeBasis.ACTUAL);

		assertThat(position.status()).isEqualTo(VehicleStatus.NOT_RUNNING);
		assertThat(position.location()).isEmpty();
	}

	@Test
	void itLeavesSeventySecondsLate() {
		VehiclePosition position = train.positionAt(at("09:33:10"), TimeBasis.ACTUAL);

		assertThat(position.status()).isEqualTo(VehicleStatus.AT_STATION);
		assertThat(position.station()).contains(ZURICH_HB);
		assertThat(position.location()).contains(ZURICH_HB.location());
		assertThat(position.delay()).contains(Delay.ofSeconds(70));
		assertThat(position.timeReliable()).isTrue();
		assertThat(position.underground()).isEqualTo(Level.UNDERGROUND);
	}

	@Test
	void halfTheTimeIsHalfTheWay() {
		VehiclePosition position = train.positionAt(at("10:00"), TimeBasis.SCHEDULED);
		Leg leg = ic712.timeline(TimeBasis.SCHEDULED).legs().getFirst();

		assertThat(position.status()).isEqualTo(VehicleStatus.MOVING);
		assertThat(position.legProgress().getAsDouble()).isCloseTo(0.5, within(1e-9));
		assertThat(position.location()).contains(leg.pointAt(0.5));
		assertThat(position.estimated()).as("phase 1 paths are straight lines").isTrue();
		assertThat(position.underground()).isEqualTo(Level.SURFACE);
	}

	@Test
	void thePlanSaysItHasArrived() {
		assertThat(train.positionAt(at("10:29"), TimeBasis.SCHEDULED).status()).isEqualTo(VehicleStatus.NOT_RUNNING);
	}

	@Test
	void inRealityItIsStillMovingOnAForecast() {
		VehiclePosition position = train.positionAt(at("10:29"), TimeBasis.ACTUAL);

		assertThat(position.status()).isEqualTo(VehicleStatus.MOVING);
		assertThat(position.delay()).contains(Delay.ofSeconds(70));
		assertThat(position.timeReliable()).as("the arrival is only a forecast").isFalse();
	}

	@Test
	void aTrainSlowsDownNearTheStations() {
		double earlyProgress = train.positionAt(at("09:42"), TimeBasis.SCHEDULED).legProgress().getAsDouble();
		double timeFraction = 10.0 / 56;

		assertThat(earlyProgress).isLessThan(timeFraction);
	}
}
