package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.vehicle.VehicleFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Makes the framework-free model classes that services need available for injection. */
@Configuration(proxyBeanMethods = false)
class ModelConfiguration {

	@Bean
	VehicleFactory vehicleFactory() {
		return new VehicleFactory();
	}
}
