package ch.usi.smb_backend.model.vehicle;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.time.TimeBasis;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovementTest {

	@Test
	void describesEachLegMovingInTheWindowWithItsCurve() {
		Vehicle train = new VehicleFactory().create(TestTrips.withCancelledStopAtAarau());

		List<Movement> planned = train.movementsBetween(at("11:00"), at("12:00"), TimeBasis.SCHEDULED);
		List<Movement> happened = train.movementsBetween(at("11:00"), at("12:00"), TimeBasis.ACTUAL);

		assertThat(planned).hasSize(2).allSatisfy(movement ->
				assertThat(movement.motionProfile()).isEqualTo(MotionProfile.SMOOTH));
		assertThat(happened).as("the cancelled stop at Aarau is passed").hasSize(1);
	}
}
