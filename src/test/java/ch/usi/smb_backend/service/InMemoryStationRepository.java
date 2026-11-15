package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.StationRepository;
import java.util.List;
import java.util.Optional;

class InMemoryStationRepository implements StationRepository {

	private final List<Station> stations;

	InMemoryStationRepository(Station... stations) {
		this.stations = List.of(stations);
	}

	@Override
	public Optional<Station> findById(StationId id) {
		return stations.stream().filter(station -> station.id().equals(id)).findFirst();
	}

	@Override
	public List<Station> findByName(String namePart) {
		return stations.stream().filter(station -> station.name().contains(namePart)).toList();
	}
}
