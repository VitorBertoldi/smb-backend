package ch.usi.smb_backend.model.vehicle;

/**
 * How a vehicle covers distance over time on a leg: turns the share of the leg's time passed
 * (0..1) into the share of its distance covered (0..1). An enum, so the API can send its name and
 * the frontend animates with exactly the same curve.
 */
public enum MotionProfile {
	/** Constant speed. */
	LINEAR {
		@Override
		public double apply(double timeFraction) {
			return timeFraction;
		}
	},
	/** Slow start, slow end (smoothstep); half the time is half the way. */
	SMOOTH {
		@Override
		public double apply(double timeFraction) {
			return timeFraction * timeFraction * (3 - 2 * timeFraction);
		}
	};

	public abstract double apply(double timeFraction);
}
