package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.MovementDto;
import ch.usi.smb_backend.controller.dto.TripDto;
import ch.usi.smb_backend.controller.dto.TripSummaryDto;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.service.ServiceDayService;
import ch.usi.smb_backend.service.TripService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * One trip at a time, fetched on demand: its stops, its legs to follow it, and a search by number.
 * A trip is addressed by its service date and its Istdaten trip id (URL-encoded: it has colons).
 */
@RestController
@RequestMapping(ApiPaths.BASE + "/trips")
class TripController {

	private final TripService trips;
	private final ServiceDayService serviceDays;

	TripController(TripService trips, ServiceDayService serviceDays) {
		this.trips = trips;
		this.serviceDays = serviceDays;
	}

	@GetMapping("/{date}/{tripId}")
	TripDto trip(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String tripId) {
		return TripDto.from(trips.trip(key(date, tripId)));
	}

	/** Every leg of one trip, as {@code /movements} describes them, so a followed train needs only this. */
	@GetMapping("/{date}/{tripId}/movements")
	List<MovementDto> movements(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String tripId) {
		return trips.movementsOf(key(date, tripId)).stream().map(MovementDto::from).toList();
	}

	/** The trips of a day with a number; a list, because operators can reuse a number. */
	@GetMapping
	List<TripSummaryDto> byNumber(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String number) {
		ServiceDay day = serviceDays.require(date);
		return trips.findByNumber(day, RequestParameters.requireText("number", number)).stream()
				.map(TripSummaryDto::from)
				.toList();
	}

	private TripKey key(LocalDate date, String tripId) {
		serviceDays.require(date);
		return new TripKey(date, tripId);
	}
}
