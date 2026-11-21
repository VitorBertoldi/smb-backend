package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "stations")
public class StationEntity {

	@Id
	@Column(name = "station_id")
	private String stationId;

	private String sloid;

	private String name;

	private double latitude;

	private double longitude;

	private Float elevation;

	// The gold database does not have stations.is_underground yet, so the level is always
	// UNKNOWN for now. Put @Column(name = "is_underground") back once the column exists.
	@Transient
	private Boolean underground;

	protected StationEntity() {
	}

	public String getStationId() {
		return stationId;
	}

	public String getSloid() {
		return sloid;
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	/** Metres above sea level; null when the stop register has none. */
	public Float getElevation() {
		return elevation;
	}

	/** Null when no OpenStreetMap stop matched. */
	public Boolean getUnderground() {
		return underground;
	}
}
