package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import java.util.List;
import java.util.Optional;

public interface StationRepository {

	Optional<Station> findById(StationId id);

	/** Stations whose name contains {@code namePart}, ignoring case, sorted by name. */
	List<Station> findByName(String namePart);
}
