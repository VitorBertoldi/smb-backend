package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Leg;
import java.util.Objects;

/** One leg a vehicle moves on, with the curve it follows: what the frontend needs to animate it. */
public record Movement(Vehicle vehicle, Leg leg) {

	public Movement {
		Objects.requireNonNull(vehicle, "vehicle");
		Objects.requireNonNull(leg, "leg");
	}

	public MotionProfile motionProfile() {
		return vehicle.motionProfile();
	}
}
