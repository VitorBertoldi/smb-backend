package ch.usi.smb_backend.repository.entity;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface StationDayStatsEntityRepository extends Repository<StationDayStatsEntity, StationDayStatsEntityId> {

	List<StationDayStatsEntity> findByIdServiceDate(LocalDate serviceDate);
}
