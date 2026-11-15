package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.controller.dto.ServiceDaysDto;
import ch.usi.smb_backend.service.ServiceDayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE)
class ServiceDayController {

	private final ServiceDayService serviceDays;

	ServiceDayController(ServiceDayService serviceDays) {
		this.serviceDays = serviceDays;
	}

	@GetMapping("/days")
	ServiceDaysDto days() {
		return new ServiceDaysDto(serviceDays.availableDates());
	}
}
