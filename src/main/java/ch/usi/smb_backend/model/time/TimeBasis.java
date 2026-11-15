package ch.usi.smb_backend.model.time;

/**
 * Which clock a calculation follows: the timetable as planned, or what happened. Every position,
 * departure board and leg is computed on one basis.
 */
public enum TimeBasis {
	SCHEDULED,
	ACTUAL
}
