package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.network.Station;
import java.util.List;

public interface StationRepository {

	/** Every station, sorted by id. */
	List<Station> findAll();
}
