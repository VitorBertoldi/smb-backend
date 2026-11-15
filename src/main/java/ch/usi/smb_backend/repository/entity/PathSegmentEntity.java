package ch.usi.smb_backend.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(name = "path_segments")
public class PathSegmentEntity {

	@Id
	@Column(name = "segment_id")
	private int segmentId;

	@Column(name = "from_station_id")
	private String fromStationId;

	@Column(name = "to_station_id")
	private String toStationId;

	private String network;

	@Column(name = "length_m")
	private int lengthM;

	/** {@code [[longitude, latitude, height_m], ...]} from the first station to the second. */
	@JdbcTypeCode(SqlTypes.JSON)
	private List<List<Double>> points;

	@Column(name = "has_tunnel")
	private Boolean hasTunnel;

	@Column(name = "is_estimated")
	private boolean estimated;

	protected PathSegmentEntity() {
	}

	public int getSegmentId() {
		return segmentId;
	}

	public String getFromStationId() {
		return fromStationId;
	}

	public String getToStationId() {
		return toStationId;
	}

	public String getNetwork() {
		return network;
	}

	public int getLengthM() {
		return lengthM;
	}

	public List<List<Double>> getPoints() {
		return points;
	}

	/** Null until the data group adds tunnels from OpenStreetMap. */
	public Boolean getHasTunnel() {
		return hasTunnel;
	}

	public boolean isEstimated() {
		return estimated;
	}
}
