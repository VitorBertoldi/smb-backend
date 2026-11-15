package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Starts and stops slowly at piers. */
public final class Boat extends Vehicle {

	public Boat(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.SMOOTH;
	}
}
