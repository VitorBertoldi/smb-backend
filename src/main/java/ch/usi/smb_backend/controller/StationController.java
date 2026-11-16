package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.StationDto;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.service.NetworkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every station with its location, loaded once by the frontend, or one station by id. */
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

	@GetMapping("/{id}")
	StationDto station(@PathVariable String id) {
		return StationDto.from(network.station(new StationId(id)));
	}
}
