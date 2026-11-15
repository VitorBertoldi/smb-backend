package ch.usi.smb_backend.controller;

import static ch.usi.smb_backend.controller.RequestParameters.clock;
import static ch.usi.smb_backend.controller.RequestParameters.orEmpty;
import static ch.usi.smb_backend.controller.RequestParameters.requireAtMost;
import static ch.usi.smb_backend.controller.RequestParameters.requireOrdered;

import ch.usi.smb_backend.controller.dto.MovementDto;
import ch.usi.smb_backend.controller.dto.VehiclePositionDto;
import ch.usi.smb_backend.model.network.VehicleType;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.service.ServiceDayService;
import ch.usi.smb_backend.service.VehicleTrackingService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every vehicle at once. {@code /vehicles} is the exact answer for one moment; {@code /movements}
 * describes each leg of a window once, so the frontend can animate it by itself.
 */
@RestController
@RequestMapping(ApiPaths.BASE)
class VehicleController {

	/** Longer windows would load most of a day's trips in one request. */
	static final Duration MAX_MOVEMENT_WINDOW = Duration.ofHours(3);

	private final VehicleTrackingService tracking;
	private final ServiceDayService serviceDays;

	VehicleController(VehicleTrackingService tracking, ServiceDayService serviceDays) {
		this.tracking = tracking;
		this.serviceDays = serviceDays;
	}

	@GetMapping("/vehicles")
	List<VehiclePositionDto> vehicles(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String time,
			@RequestParam(defaultValue = "actual") TimeBasis basis,
			@RequestParam(name = "type", required = false) Set<VehicleType> types) {
		ServiceDay day = serviceDays.require(date);
		return tracking.positionsAt(day, clock(day, "time", time), basis, orEmpty(types)).stream()
				.map(VehiclePositionDto::from)
				.toList();
	}

	@GetMapping("/movements")
	List<MovementDto> movements(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam(defaultValue = "actual") TimeBasis basis,
			@RequestParam(name = "type", required = false) Set<VehicleType> types) {
		ServiceDay day = serviceDays.require(date);
		ServiceTime windowStart = clock(day, "from", from);
		ServiceTime windowEnd = clock(day, "to", to);
		requireOrdered(windowStart, windowEnd);
		requireAtMost(windowStart, windowEnd, MAX_MOVEMENT_WINDOW);
		return tracking.movementsBetween(day, windowStart, windowEnd, basis, orEmpty(types)).stream()
				.map(MovementDto::from)
				.toList();
	}
}
