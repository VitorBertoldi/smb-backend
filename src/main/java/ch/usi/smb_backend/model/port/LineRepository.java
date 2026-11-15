package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.network.Line;
import java.util.List;

public interface LineRepository {

	/** Every line with exactly this name (one per operator), ignoring case. */
	List<Line> findByName(String name);
}
