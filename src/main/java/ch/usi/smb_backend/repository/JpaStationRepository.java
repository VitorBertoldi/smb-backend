package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.StationRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Stations, answered from the in-memory reference data. */
@Repository
class JpaStationRepository implements StationRepository {

	private final ReferenceDataCache reference;

	JpaStationRepository(ReferenceDataCache reference) {
		this.reference = reference;
	}

	@Override
	public Optional<Station> findById(StationId id) {
		return reference.get().findStation(id);
	}

	@Override
	public List<Station> findByName(String namePart) {
		String wanted = namePart.toLowerCase(Locale.ROOT);
		return reference.get().allStations().stream()
				.filter(station -> station.name().toLowerCase(Locale.ROOT).contains(wanted))
				.sorted(Comparator.comparing(Station::name))
				.toList();
	}
}
