package ch.usi.smb_backend.model.vehicle;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestNetwork;
import ch.usi.smb_backend.model.fixtures.TripBuilder;
import ch.usi.smb_backend.model.network.VehicleType;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VehicleFactoryTest {

	private static final Map<VehicleType, Class<? extends Vehicle>> EXPECTED = Map.of(
			VehicleType.TRAIN, Train.class,
			VehicleType.RACK_RAILWAY, Train.class,
			VehicleType.TRAM, Tram.class,
			VehicleType.METRO, Tram.class,
			VehicleType.BUS, Bus.class,
			VehicleType.BOAT, Boat.class,
			VehicleType.FUNICULAR, Funicular.class,
			VehicleType.CABLE_CAR, CableCar.class);

	private static final Map<VehicleType, MotionProfile> EXPECTED_MOTION = Map.of(
			VehicleType.TRAIN, MotionProfile.SMOOTH,
			VehicleType.RACK_RAILWAY, MotionProfile.SMOOTH,
			VehicleType.TRAM, MotionProfile.SMOOTH,
			VehicleType.METRO, MotionProfile.SMOOTH,
			VehicleType.BUS, MotionProfile.SMOOTH,
			VehicleType.BOAT, MotionProfile.SMOOTH,
			VehicleType.FUNICULAR, MotionProfile.LINEAR,
			VehicleType.CABLE_CAR, MotionProfile.LINEAR);

	@ParameterizedTest
	@EnumSource(VehicleType.class)
	void createsTheVehicleThatMovesLikeTheType(VehicleType type) {
		Vehicle vehicle = new VehicleFactory().create(TripBuilder.trip("t")
				.on(TestNetwork.line("L", "X", type))
				.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30"))
				.build());

		assertThat(vehicle).isExactlyInstanceOf(EXPECTED.get(type));
		assertThat(vehicle.motionProfile()).isEqualTo(EXPECTED_MOTION.get(type));
	}
}
