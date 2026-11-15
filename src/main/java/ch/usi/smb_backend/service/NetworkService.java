package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.LineRepository;
import ch.usi.smb_backend.model.port.PathSegmentRepository;
import ch.usi.smb_backend.model.port.StationRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/** Stations, lines and paths: the reference data. */
@Service
public class NetworkService {

	private final StationRepository stations;
	private final LineRepository lines;
	private final PathSegmentRepository segments;

	public NetworkService(StationRepository stations, LineRepository lines, PathSegmentRepository segments) {
		this.stations = stations;
		this.lines = lines;
		this.segments = segments;
	}

	/**
	 * @throws NotFoundException when there's no such station
	 */
	public Station station(StationId id) {
		return stations.findById(id).orElseThrow(() -> new NotFoundException("no station " + id));
	}

	public List<Station> stationsNamed(String namePart) {
		return stations.findByName(namePart);
	}

	public List<Line> linesNamed(String name) {
		return lines.findByName(name);
	}

	/** The paths with these ids, in the order asked; unknown ids are left out. */
	public List<PathSegment> paths(Collection<Integer> ids) {
		return segments.findByIds(ids);
	}
}
