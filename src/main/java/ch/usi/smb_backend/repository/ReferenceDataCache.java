package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.repository.entity.LineEntityRepository;
import ch.usi.smb_backend.repository.entity.PathSegmentEntityRepository;
import ch.usi.smb_backend.repository.entity.StationEntityRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Loads the reference data on first use and keeps it for the life of the application. */
@Component
class ReferenceDataCache {

	private static final Logger LOG = LoggerFactory.getLogger(ReferenceDataCache.class);

	private final StationEntityRepository stationRows;
	private final LineEntityRepository lineRows;
	private final PathSegmentEntityRepository segmentRows;
	private volatile ReferenceData data;

	ReferenceDataCache(
			StationEntityRepository stationRows,
			LineEntityRepository lineRows,
			PathSegmentEntityRepository segmentRows) {
		this.stationRows = stationRows;
		this.lineRows = lineRows;
		this.segmentRows = segmentRows;
	}

	ReferenceData get() {
		ReferenceData loaded = data;
		if (loaded == null) {
			synchronized (this) {
				if (data == null) {
					data = load();
				}
				loaded = data;
			}
		}
		return loaded;
	}

	private ReferenceData load() {
		Map<StationId, Station> stations = stationRows.findAll().stream()
				.map(ReferenceDataMapper::toStation)
				.collect(Collectors.toMap(Station::id, Function.identity()));
		Map<String, Line> lines = lineRows.findAll().stream()
				.map(ReferenceDataMapper::toLine)
				.collect(Collectors.toMap(Line::id, Function.identity()));
		Map<Integer, PathSegment> segments = segmentRows.findAll().stream()
				.map(row -> ReferenceDataMapper.toSegment(row, stations))
				.collect(Collectors.toMap(PathSegment::id, Function.identity()));
		LOG.info("Loaded {} stations, {} lines and {} path segments", stations.size(), lines.size(), segments.size());
		return new ReferenceData(stations, lines, segments);
	}
}
