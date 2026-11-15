package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Moves like a tram, on roads. */
public final class Bus extends Vehicle {

	public Bus(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.SMOOTH;
	}
}
