package ch.usi.smb_backend.model.vehicle;

public enum VehicleStatus {
	/** Before the first departure, after the last arrival, or not existing on this basis. */
	NOT_RUNNING,
	/** Between arriving at and leaving a stop. */
	AT_STATION,
	/** On a leg between two stops. */
	MOVING
}
