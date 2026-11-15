package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "lines")
public class LineEntity {

	@Id
	@Column(name = "line_id")
	private String lineId;

	private String name;

	private String category;

	@Column(name = "vehicle_type")
	private String vehicleType;

	@Column(name = "agency_name")
	private String agencyName;

	@Column(name = "agency_code")
	private String agencyCode;

	protected LineEntity() {
	}

	public String getLineId() {
		return lineId;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public String getAgencyCode() {
		return agencyCode;
	}
}
