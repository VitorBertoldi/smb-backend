package ch.usi.smb_backend.model.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The shape of a path: its points, plus the distance from the start to every point, calculated
 * once when it is built.
 */
public final class Polyline {

	private final List<GeoPoint> points;
	private final double[] distancesM;

	private Polyline(List<GeoPoint> points) {
		if (points.isEmpty()) {
			throw new IllegalArgumentException("a polyline needs at least one point");
		}
		this.points = List.copyOf(points);
		this.distancesM = cumulativeDistances(this.points);
	}

	public static Polyline of(List<GeoPoint> points) {
		return new Polyline(Objects.requireNonNull(points));
	}

	public static Polyline straight(GeoPoint from, GeoPoint to) {
		return new Polyline(List.of(from, to));
	}

	public List<GeoPoint> points() {
		return points;
	}

	public double lengthM() {
		return distancesM[distancesM.length - 1];
	}

	/** The point {@code distanceM} along the line, interpolated; clamped to the two ends. */
	public GeoPoint pointAt(double distanceM) {
		if (distanceM <= 0 || points.size() == 1) {
			return points.getFirst();
		}
		if (distanceM >= lengthM()) {
			return points.getLast();
		}
		int end = firstIndexBeyond(distanceM);
		int start = end - 1;
		double spanM = distancesM[end] - distancesM[start];
		double fraction = (distanceM - distancesM[start]) / spanM;
		return points.get(start).towards(points.get(end), fraction);
	}

	/** This line followed by {@code next}; a point shared by both ends is kept once. */
	public Polyline then(Polyline next) {
		List<GeoPoint> joined = new ArrayList<>(points);
		List<GeoPoint> rest = next.points;
		boolean sharesJoint = joined.getLast().equals(rest.getFirst());
		joined.addAll(sharesJoint ? rest.subList(1, rest.size()) : rest);
		return new Polyline(joined);
	}

	/** Index of the first point further along than {@code distanceM}; 0 < distance < length. */
	private int firstIndexBeyond(double distanceM) {
		int index = Arrays.binarySearch(distancesM, distanceM);
		return index >= 0 ? nextDistinctPoint(index) : -index - 1;
	}

	private int nextDistinctPoint(int index) {
		int next = index + 1;
		while (distancesM[next] == distancesM[index]) {
			next++;
		}
		return next;
	}

	private static double[] cumulativeDistances(List<GeoPoint> points) {
		double[] distances = new double[points.size()];
		for (int i = 1; i < points.size(); i++) {
			distances[i] = distances[i - 1] + points.get(i - 1).distanceTo(points.get(i));
		}
		return distances;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Polyline polyline && points.equals(polyline.points);
	}

	@Override
	public int hashCode() {
		return points.hashCode();
	}

	@Override
	public String toString() {
		return "Polyline[" + points.size() + " points, " + Math.round(lengthM()) + " m]";
	}
}
