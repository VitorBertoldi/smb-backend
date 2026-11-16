package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.StationDto;
import ch.usi.smb_backend.service.NetworkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every station with its location: the frontend loads them once and looks them up by id. */
@RestController
@RequestMapping(ApiPaths.BASE + "/stations")
class StationController {

	private final NetworkService network;

	StationController(NetworkService network) {
		this.network = network;
	}

	@GetMapping
	List<StationDto> stations() {
		return network.allStations().stream().map(StationDto::from).toList();
	}
}
