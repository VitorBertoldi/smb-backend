package ch.usi.smb_backend.controller;

import static ch.usi.smb_backend.controller.RequestParameters.clock;
import static ch.usi.smb_backend.controller.RequestParameters.requireAtMost;
import static ch.usi.smb_backend.controller.RequestParameters.requireOrdered;

import ch.usi.smb_backend.controller.dto.MovementDto;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.service.ServiceDayService;
import ch.usi.smb_backend.service.VehicleTrackingService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * What moves in a time window: each leg once, with its times, paths and motion curve, so the
 * frontend can animate every vehicle by itself.
 */
@RestController
@RequestMapping(ApiPaths.BASE)
class MovementController {

	/** Longer windows would load most of a day's trips in one request. */
	static final Duration MAX_WINDOW = Duration.ofHours(3);

	private final VehicleTrackingService tracking;
	private final ServiceDayService serviceDays;

	MovementController(VehicleTrackingService tracking, ServiceDayService serviceDays) {
		this.tracking = tracking;
		this.serviceDays = serviceDays;
	}

	@GetMapping("/movements")
	List<MovementDto> movements(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String from,
			@RequestParam String to) {
		ServiceDay day = serviceDays.require(date);
		ServiceTime windowStart = clock(day, "from", from);
		ServiceTime windowEnd = clock(day, "to", to);
		requireOrdered(windowStart, windowEnd);
		requireAtMost(windowStart, windowEnd, MAX_WINDOW);
		return tracking.movementsBetween(day, windowStart, windowEnd).stream()
				.map(MovementDto::from)
				.toList();
	}
}
