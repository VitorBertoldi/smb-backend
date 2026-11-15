package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Moves at constant speed on its rope. */
public final class CableCar extends Vehicle {

	public CableCar(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.LINEAR;
	}
}
