package ch.usi.smb_backend.model.fixtures;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU_TO_BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_TO_AARAU;

import ch.usi.smb_backend.model.time.TimeStatus;
import ch.usi.smb_backend.model.timetable.Trip;

/** The trips of the domain model's test section, built by hand. Times are invented for the tests. */
public final class TestTrips {

	private TestTrips() {
	}

	/**
	 * IC 712: leaves Zürich HB 70 s late (measured), passes Aarau without stopping, and is
	 * forecast to reach Bern 2:40 late.
	 */
	public static Trip ic712() {
		return TripBuilder.trip("85:11:712:001").number("712")
				.stops(
						StopEventBuilder.at(ZURICH_HB).departs("09:32", "09:33:10", TimeStatus.MEASURED)
								.onPlatform("ch:1:sloid:3000:503:34").then(ZURICH_TO_AARAU),
						StopEventBuilder.at(AARAU).arrives("10:00", "10:01", TimeStatus.MEASURED)
								.departs("10:00", "10:01", TimeStatus.MEASURED).passThrough().then(AARAU_TO_BERN),
						StopEventBuilder.at(BERN).arrives("10:28", "10:30:40", TimeStatus.FORECAST))
				.build();
	}

	/** Zürich HB, Aarau, Bern with a real stop at Aarau, which is cancelled on the day. */
	public static Trip withCancelledStopAtAarau() {
		return TripBuilder.trip("85:11:2512:001").number("2512")
				.stops(
						StopEventBuilder.at(ZURICH_HB).departs("11:00").then(ZURICH_TO_AARAU),
						StopEventBuilder.at(AARAU).arrives("11:30").departs("11:32").cancelled().then(AARAU_TO_BERN),
						StopEventBuilder.at(BERN).arrives("12:00"))
				.build();
	}
}
