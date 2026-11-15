package ch.usi.smb_backend.repository;

import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.model.port.StationStatsRepository;
import ch.usi.smb_backend.model.statistics.StationDayStats;
import ch.usi.smb_backend.model.time.Delay;
import ch.usi.smb_backend.repository.entity.StationDayStatsEntity;
import ch.usi.smb_backend.repository.entity.StationDayStatsEntityRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
class JpaStationStatsRepository implements StationStatsRepository {

	private static final double PERCENT = 100.0;

	private final StationDayStatsEntityRepository statsRows;
	private final ReferenceDataCache reference;

	JpaStationStatsRepository(StationDayStatsEntityRepository statsRows, ReferenceDataCache reference) {
		this.statsRows = statsRows;
		this.reference = reference;
	}

	@Override
	public List<StationDayStats> findByDay(LocalDate serviceDate) {
		ReferenceData data = reference.get();
		return statsRows.findByIdServiceDate(serviceDate).stream()
				.map(row -> toStats(row, data))
				.toList();
	}

	private static StationDayStats toStats(StationDayStatsEntity row, ReferenceData data) {
		Float percentOnTime = row.getPercentOnTime();
		return new StationDayStats(
				data.station(new StationId(row.getId().stationId())),
				row.getId().serviceDate(),
				row.getEvents(),
				row.getCancelled(),
				Optional.ofNullable(row.getAverageDepartureDelaySeconds()).map(Delay::ofSeconds),
				percentOnTime == null ? OptionalDouble.empty() : OptionalDouble.of(percentOnTime / PERCENT));
	}
}
