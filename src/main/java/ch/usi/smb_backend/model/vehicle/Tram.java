package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/** Moves like a train, with short hops (trams and metros). */
public final class Tram extends Vehicle {

	public Tram(Trip trip) {
		super(trip);
	}

	@Override
	public MotionProfile motionProfile() {
		return MotionProfile.SMOOTH;
	}
}
