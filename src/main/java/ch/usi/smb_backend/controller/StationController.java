package ch.usi.smb_backend.controller;

import static ch.usi.smb_backend.controller.RequestParameters.clock;
import static ch.usi.smb_backend.controller.RequestParameters.requireOrdered;
import static ch.usi.smb_backend.controller.RequestParameters.requireText;

import ch.usi.smb_backend.controller.dto.DepartureDto;
import ch.usi.smb_backend.controller.dto.StationDto;
import ch.usi.smb_backend.controller.dto.StationStatsDto;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import ch.usi.smb_backend.model.time.TimeBasis;
import ch.usi.smb_backend.service.DepartureBoardService;
import ch.usi.smb_backend.service.NetworkService;
import ch.usi.smb_backend.service.ServiceDayService;
import ch.usi.smb_backend.service.StationStatsService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE + "/stations")
class StationController {

	private final NetworkService network;
	private final ServiceDayService serviceDays;
	private final DepartureBoardService departureBoard;
	private final StationStatsService stationStats;

	StationController(
			NetworkService network,
			ServiceDayService serviceDays,
			DepartureBoardService departureBoard,
			StationStatsService stationStats) {
		this.network = network;
		this.serviceDays = serviceDays;
		this.departureBoard = departureBoard;
		this.stationStats = stationStats;
	}

	@GetMapping
	List<StationDto> stationsNamed(@RequestParam String name) {
		return network.stationsNamed(requireText("name", name)).stream().map(StationDto::from).toList();
	}

	@GetMapping("/{id}")
	StationDto station(@PathVariable String id) {
		return StationDto.from(network.station(new StationId(id)));
	}

	@GetMapping("/{id}/departures")
	List<DepartureDto> departures(
			@PathVariable String id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam(defaultValue = "actual") TimeBasis basis) {
		ServiceDay day = serviceDays.require(date);
		ServiceTime windowStart = clock(day, "from", from);
		ServiceTime windowEnd = clock(day, "to", to);
		requireOrdered(windowStart, windowEnd);
		return departureBoard.departures(new StationId(id), day, windowStart, windowEnd, basis).stream()
				.map(departure -> DepartureDto.from(departure, day))
				.toList();
	}

	@GetMapping("/stats")
	List<StationStatsDto> stats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return stationStats.statsOn(date).stream().map(StationStatsDto::from).toList();
	}
}
