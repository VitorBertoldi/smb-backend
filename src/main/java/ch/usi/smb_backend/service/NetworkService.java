package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.port.PathSegmentRepository;
import ch.usi.smb_backend.model.port.StationRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/** Stations and paths: the geography the frontend loads once and caches. */
@Service
public class NetworkService {

	private final StationRepository stations;
	private final PathSegmentRepository segments;

	public NetworkService(StationRepository stations, PathSegmentRepository segments) {
		this.stations = stations;
		this.segments = segments;
	}

	/** Every station, sorted by id. */
	public List<Station> allStations() {
		return stations.findAll();
	}

	/** The paths with these ids, in the order asked; unknown ids are left out. */
	public List<PathSegment> paths(Collection<Integer> ids) {
		return segments.findByIds(ids);
	}
}
