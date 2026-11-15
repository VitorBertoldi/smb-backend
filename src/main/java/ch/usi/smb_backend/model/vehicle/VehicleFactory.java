package ch.usi.smb_backend.model.vehicle;

import ch.usi.smb_backend.model.timetable.Trip;

/**
 * The one place that decides which kind of vehicle runs a trip. The compiler checks the switch
 * covers every vehicle type.
 */
public final class VehicleFactory {

	public Vehicle create(Trip trip) {
		return switch (trip.line().vehicleType()) {
			case TRAIN, RACK_RAILWAY -> new Train(trip);
			case TRAM, METRO -> new Tram(trip);
			case BUS -> new Bus(trip);
			case BOAT -> new Boat(trip);
			case FUNICULAR -> new Funicular(trip);
			case CABLE_CAR -> new CableCar(trip);
		};
	}
}
