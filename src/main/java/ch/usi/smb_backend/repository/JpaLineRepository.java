package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.port.LineRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Lines, answered from the in-memory reference data. */
@Repository
class JpaLineRepository implements LineRepository {

	private final ReferenceDataCache reference;

	JpaLineRepository(ReferenceDataCache reference) {
		this.reference = reference;
	}

	@Override
	public List<Line> findByName(String name) {
		return reference.get().allLines().stream()
				.filter(line -> line.name().equalsIgnoreCase(name))
				.sorted(Comparator.comparing(Line::id))
				.toList();
	}
}
