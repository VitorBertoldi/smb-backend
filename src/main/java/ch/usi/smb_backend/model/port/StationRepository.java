package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import java.util.List;
import java.util.Optional;

public interface StationRepository {

	/** Every station, sorted by id. */
	List<Station> findAll();

	/** One station, or empty when the id is unknown. */
	Optional<Station> findById(StationId id);
}
