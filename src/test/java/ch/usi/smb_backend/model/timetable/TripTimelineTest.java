package ch.usi.smb_backend.model.timetable;

import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.AARAU_TO_BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.BERN;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_HB;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.ZURICH_TO_AARAU;
import static ch.usi.smb_backend.model.fixtures.TestNetwork.at;
import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.model.fixtures.StopEventBuilder;
import ch.usi.smb_backend.model.fixtures.TestTrips;
import ch.usi.smb_backend.model.fixtures.TripBuilder;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.time.TimeStatus;
import org.junit.jupiter.api.Test;

class TripTimelineTest {

	@Test
	void aPassedStationIsPartOfALongerLeg() {
		TripTimeline timeline = TestTrips.ic712().timeline(TimeBasis.SCHEDULED);

		assertThat(timeline.stops()).extracting(Stop::station).containsExactly(ZURICH_HB, BERN);
		assertThat(timeline.legs()).singleElement()
				.satisfies(leg -> assertThat(leg.segments()).containsExactly(ZURICH_TO_AARAU, AARAU_TO_BERN));
	}

	@Test
	void followsThePlanOrWhatHappened() {
		Trip ic712 = TestTrips.ic712();

		assertThat(ic712.timeline(TimeBasis.SCHEDULED).firstDeparture()).isEqualTo(at("09:32"));
		assertThat(ic712.timeline(TimeBasis.ACTUAL).firstDeparture()).isEqualTo(at("09:33:10"));
		assertThat(ic712.timeline(TimeBasis.ACTUAL).lastArrival()).isEqualTo(at("10:30:40"));
	}

	@Test
	void aCancelledStopIsMadeInThePlanAndPassedInReality() {
		Trip trip = TestTrips.withCancelledStopAtAarau();

		assertThat(trip.timeline(TimeBasis.SCHEDULED).stops()).extracting(Stop::station)
				.containsExactly(ZURICH_HB, AARAU, BERN);
		assertThat(trip.timeline(TimeBasis.ACTUAL).stops()).extracting(Stop::station)
				.containsExactly(ZURICH_HB, BERN);
	}

	@Test
	void raisesActualTimesThatComeOutOfOrder() {
		Trip measuredBadly = TripBuilder.trip("t")
				.stops(StopEventBuilder.at(ZURICH_HB).departs("10:00", "10:05", TimeStatus.MEASURED).then(ZURICH_TO_AARAU),
						StopEventBuilder.at(AARAU).arrives("10:30", "10:03", TimeStatus.MEASURED)
								.departs("10:31", "10:02", TimeStatus.MEASURED).then(AARAU_TO_BERN),
						StopEventBuilder.at(BERN).arrives("11:00", "11:01", TimeStatus.MEASURED))
				.build();

		Stop aarau = measuredBadly.timeline(TimeBasis.ACTUAL).stops().get(1);

		assertThat(aarau.arrival()).isEqualTo(at("10:05"));
		assertThat(aarau.departure()).isEqualTo(at("10:05"));
	}

	@Test
	void findsTheStopOrTheLegAtATime() {
		TripTimeline timeline = TestTrips.withCancelledStopAtAarau().timeline(TimeBasis.SCHEDULED);

		assertThat(timeline.stopAt(at("11:31"))).map(Stop::station).contains(AARAU);
		assertThat(timeline.legAt(at("11:31"))).isEmpty();
		assertThat(timeline.legAt(at("11:15"))).map(leg -> leg.to().station()).contains(AARAU);
		assertThat(timeline.stopAt(at("11:15"))).isEmpty();
		assertThat(timeline.legAt(at("11:00"))).as("the departure moment belongs to the station").isEmpty();
	}

	@Test
	void isNotRunningOutsideItsTimes() {
		TripTimeline timeline = TestTrips.ic712().timeline(TimeBasis.SCHEDULED);

		assertThat(timeline.isRunningAt(at("09:31"))).isFalse();
		assertThat(timeline.isRunningAt(at("09:32"))).isTrue();
		assertThat(timeline.isRunningAt(at("10:28"))).isTrue();
		assertThat(timeline.isRunningAt(at("10:29"))).isFalse();
		assertThat(timeline.stopAt(at("10:29"))).isEmpty();
	}

	@Test
	void theLastOfSeveralMatchingStopsWins() {
		Trip quickHops = TripBuilder.trip("bus")
				.stops(StopEventBuilder.at(ZURICH_HB).departs("10:00"),
						StopEventBuilder.at(AARAU).arrives("10:00").departs("10:00"),
						StopEventBuilder.at(BERN).arrives("10:00"))
				.build();

		assertThat(quickHops.timeline(TimeBasis.SCHEDULED).stopAt(at("10:00"))).map(Stop::station).contains(BERN);
	}

	@Test
	void findsTheNextStop() {
		TripTimeline timeline = TestTrips.withCancelledStopAtAarau().timeline(TimeBasis.SCHEDULED);

		assertThat(timeline.nextStopAfter(at("11:10"))).map(Stop::station).contains(AARAU);
		assertThat(timeline.nextStopAfter(at("11:31"))).map(Stop::station).contains(BERN);
		assertThat(timeline.nextStopAfter(at("12:00"))).isEmpty();
	}

	@Test
	void listsTheLegsMovingInAWindow() {
		TripTimeline timeline = TestTrips.withCancelledStopAtAarau().timeline(TimeBasis.SCHEDULED);

		assertThat(timeline.legsMovingBetween(at("11:20"), at("11:40"))).hasSize(2);
		assertThat(timeline.legsMovingBetween(at("11:30"), at("11:32"))).as("standing at Aarau").isEmpty();
		assertThat(timeline.legsMovingBetween(at("12:00"), at("12:30"))).isEmpty();
	}

	@Test
	void aCancelledTripHasNoMovementsInReality() {
		Trip cancelled = TripBuilder.trip("c").cancelled()
				.stops(StopEventBuilder.at(AARAU).departs("10:00"), StopEventBuilder.at(BERN).arrives("10:30")).build();

		assertThat(cancelled.timeline(TimeBasis.ACTUAL).isRunningAt(at("10:10"))).isFalse();
		assertThat(cancelled.timeline(TimeBasis.ACTUAL).legsMovingBetween(at("10:00"), at("10:30"))).isEmpty();
		assertThat(cancelled.timeline(TimeBasis.SCHEDULED).isRunningAt(at("10:10"))).isTrue();
	}
}
