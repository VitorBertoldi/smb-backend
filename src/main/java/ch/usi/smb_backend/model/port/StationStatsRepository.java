package ch.usi.smb_backend.model.port;

import ch.usi.smb_backend.model.statistics.StationDayStats;
import java.time.LocalDate;
import java.util.List;

public interface StationStatsRepository {

	List<StationDayStats> findByDay(LocalDate serviceDate);
}
