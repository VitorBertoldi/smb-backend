package ch.usi.smb_backend.service;

import ch.usi.smb_backend.model.port.StationStatsRepository;
import ch.usi.smb_backend.model.statistics.StationDayStats;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** How each station did on a day, for the map. */
@Service
public class StationStatsService {

	private final StationStatsRepository stats;
	private final ServiceDayService serviceDays;

	public StationStatsService(StationStatsRepository stats, ServiceDayService serviceDays) {
		this.stats = stats;
		this.serviceDays = serviceDays;
	}

	/**
	 * @throws UnknownServiceDateException when the database has no trips on that date
	 */
	public List<StationDayStats> statsOn(LocalDate date) {
		serviceDays.require(date);
		return stats.findByDay(date);
	}
}
