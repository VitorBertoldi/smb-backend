package ch.usi.smb_backend.controller.dto;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.VehicleType;

public record LineDto(String id, String name, String category, VehicleType vehicleType, String operatorCode, String operatorName) {

	public static LineDto from(Line line) {
		return new LineDto(line.id(), line.name(), line.category(), line.vehicleType(),
				line.operator().code(), line.operator().name());
	}
}
