package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.port.StationRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Stations, answered from the in-memory reference data. */
@Repository
class JpaStationRepository implements StationRepository {

	private final ReferenceDataCache reference;

	JpaStationRepository(ReferenceDataCache reference) {
		this.reference = reference;
	}

	@Override
	public List<Station> findAll() {
		return reference.get().allStations().stream()
				.sorted(Comparator.comparing(station -> station.id().code()))
				.toList();
	}
}
