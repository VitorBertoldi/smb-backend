package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.network.PathSegment;
import java.util.Collection;
import java.util.List;

public interface PathSegmentRepository {

	/** The segments with these ids, in the order asked; unknown ids are left out. */
	List<PathSegment> findByIds(Collection<Integer> ids);
}
