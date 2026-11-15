package ch.usi.smb_backend.controller;

import static ch.usi.smb_backend.controller.RequestParameters.requireText;

import ch.usi.smb_backend.controller.dto.LineDto;
import ch.usi.smb_backend.service.NetworkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE + "/lines")
class LineController {

	private final NetworkService network;

	LineController(NetworkService network) {
		this.network = network;
	}

	/** One line per operator using that name. */
	@GetMapping
	List<LineDto> linesNamed(@RequestParam String name) {
		return network.linesNamed(requireText("name", name)).stream().map(LineDto::from).toList();
	}
}
