package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Speeds up and slows down at stations. */
public final class Train extends Vehicle {

	public Train(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.SMOOTH;
	}
}
