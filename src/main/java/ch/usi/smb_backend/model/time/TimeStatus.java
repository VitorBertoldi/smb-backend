package ch.usi.smb_backend.model.time;

/** How much an actual time can be trusted (the Istdaten status columns). */
public enum TimeStatus {
	/** Recorded by the vehicle or the track (Istdaten {@code REAL}). */
	MEASURED,
	/** Estimated by the operator after the fact ({@code GESCHAETZT}). */
	ESTIMATED,
	/** A prediction made before it happened, never confirmed ({@code PROGNOSE}). */
	FORECAST,
	/** No actual time: only the plan is known ({@code UNBEKANNT} or empty). */
	UNKNOWN;

	public boolean isReliable() {
		return this == MEASURED || this == ESTIMATED;
	}
}
