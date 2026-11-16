package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.PathDto;
import ch.usi.smb_backend.service.NetworkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	/** Longer lists would not fit in a URL anyway; ask in batches. */
	static final int MAX_IDS = 200;

	/** One path by id: a short URL the browser and any proxy can cache for good. */
	@GetMapping("/{id}")
	PathDto path(@PathVariable int id) {
		return PathDto.from(network.path(id));
	}

	/** Several paths at once ({@code ids=109,116}), at most {@value #MAX_IDS}; unknown ids are left out. */
	@GetMapping
	List<PathDto> paths(@RequestParam List<Integer> ids) {
		if (ids.size() > MAX_IDS) {
			throw new BadRequestException("'ids' may hold at most " + MAX_IDS + " ids");
		}
		return network.paths(ids).stream().map(PathDto::from).toList();
	}
}
