package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.PathDto;
import ch.usi.smb_backend.service.NetworkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE + "/paths")
class PathController {

	private final NetworkService network;

	PathController(NetworkService network) {
		this.network = network;
	}

	/** Several paths at once ({@code ids=109,116}), so the frontend can cache them; unknown ids are left out. */
	@GetMapping
	List<PathDto> paths(@RequestParam List<Integer> ids) {
		return network.paths(ids).stream().map(PathDto::from).toList();
	}
}
