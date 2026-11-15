package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Pulled by a cable, at a steady speed. */
public final class Funicular extends Vehicle {

	public Funicular(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.LINEAR;
	}
}
