package ch.usi.smb_backend.controller;

import static ch.usi.smb_backend.controller.RequestParameters.clock;

import ch.usi.smb_backend.controller.dto.TripDto;
import ch.usi.smb_backend.controller.dto.VehiclePositionDto;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.model.timetable.TripKey;
import ch.usi.smb_backend.service.ServiceDayService;
import ch.usi.smb_backend.service.TripService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** A trip is addressed by its service date and its Istdaten trip id (URL-encoded: it has colons). */
@RestController
@RequestMapping(ApiPaths.BASE + "/trips/{date}/{tripId}")
class TripController {

	private final TripService trips;
	private final ServiceDayService serviceDays;

	TripController(TripService trips, ServiceDayService serviceDays) {
		this.trips = trips;
		this.serviceDays = serviceDays;
	}

	@GetMapping
	TripDto trip(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String tripId) {
		return TripDto.from(trips.trip(new TripKey(date, tripId)));
	}

	@GetMapping("/position")
	VehiclePositionDto position(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@PathVariable String tripId,
			@RequestParam String time,
			@RequestParam(defaultValue = "actual") TimeBasis basis) {
		ServiceDay day = serviceDays.require(date);
		return VehiclePositionDto.from(trips.position(new TripKey(date, tripId), clock(day, "time", time), basis));
	}
}
