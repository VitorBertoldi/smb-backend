package ch.usi.smb_backend.model.fixtures;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.geo.Polyline;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.NetworkType;
import ch.usi.smb_backend.model.network.Operator;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import java.time.LocalDate;

/** A small, hand-made piece of the Swiss network for model tests. */
public final class TestNetwork {

	public static final ServiceDay WEDNESDAY = ServiceDay.of(LocalDate.of(2026, 10, 21));

	public static final Operator SBB = new Operator("11", "Schweizerische Bundesbahnen SBB");

	public static final Station ZURICH_HB = station("8503000", "Zürich HB", 8.5402, 47.3782, 408, Level.UNDERGROUND);
	public static final Station AARAU = station("8502113", "Aarau", 8.0510, 47.3913, 383, Level.SURFACE);
	public static final Station BERN = station("8507000", "Bern", 7.4391, 46.9490, 540, Level.SURFACE);

	public static final PathSegment ZURICH_TO_AARAU = straightSegment(1, ZURICH_HB, AARAU);
	public static final PathSegment AARAU_TO_BERN = straightSegment(2, AARAU, BERN);
	public static final PathSegment BERN_TO_AARAU = straightSegment(3, BERN, AARAU);

	private TestNetwork() {
	}

	/** A service time on {@link #WEDNESDAY}, from wall-clock text such as {@code "09:32"}. */
	public static ServiceTime at(String clock) {
		return WEDNESDAY.parseClock(clock);
	}

	public static Line line(String name, String category, VehicleType vehicleType) {
		return new Line("11:" + name, name, category, vehicleType, SBB);
	}

	public static Line intercity() {
		return line("IC1", "IC", VehicleType.TRAIN);
	}

	public static Station station(String code, String name, double longitude, double latitude, double heightM, Level level) {
		return new Station(new StationId(code), "ch:1:sloid:" + code, name, new GeoPoint(longitude, latitude, heightM), level);
	}

	public static PathSegment straightSegment(int id, Station from, Station to) {
		return new PathSegment(id, from, to, NetworkType.TRACK,
				Polyline.straight(from.location(), to.location()), true, false);
	}

	public static PathSegment trackWithTunnel(int id, Station from, Station to) {
		return new PathSegment(id, from, to, NetworkType.TRACK,
				Polyline.straight(from.location(), to.location()), false, true);
	}
}
