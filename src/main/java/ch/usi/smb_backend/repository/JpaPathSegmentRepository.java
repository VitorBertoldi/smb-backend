package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.port.PathSegmentRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Path segments, answered from the in-memory reference data. */
@Repository
class JpaPathSegmentRepository implements PathSegmentRepository {

	private final ReferenceDataCache reference;

	JpaPathSegmentRepository(ReferenceDataCache reference) {
		this.reference = reference;
	}

	@Override
	public List<PathSegment> findByIds(Collection<Integer> ids) {
		ReferenceData data = reference.get();
		return ids.stream()
				.map(data::findSegment)
				.flatMap(Optional::stream)
				.toList();
	}
}
