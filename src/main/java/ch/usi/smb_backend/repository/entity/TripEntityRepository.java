package ch.usi.smb_backend.repository.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Every query fetches the trips' stop events in the same round trip. */
public interface TripEntityRepository extends Repository<TripEntity, TripEntityId> {

	@Query("select distinct trip.id.serviceDate from TripEntity trip order by trip.id.serviceDate")
	List<LocalDate> findServiceDates();

	@Query("""
			select trip from TripEntity trip join fetch trip.stopEvents
			where trip.id = :id""")
	Optional<TripEntity> findWithStopEvents(@Param("id") TripEntityId id);

	@Query("""
			select distinct trip from TripEntity trip join fetch trip.stopEvents
			where trip.id.serviceDate = :serviceDate
			  and trip.firstDeparture <= :windowEnd
			  and trip.lastArrival >= :windowStart""")
	List<TripEntity> findOverlapping(
			@Param("serviceDate") LocalDate serviceDate,
			@Param("windowStart") OffsetDateTime windowStart,
			@Param("windowEnd") OffsetDateTime windowEnd);

	@Query("""
			select distinct trip from TripEntity trip join fetch trip.stopEvents
			where trip.id.serviceDate = :serviceDate
			  and exists (
			      select 1 from StopEventEntity event
			      where event.trip = trip
			        and event.stationId = :stationId
			        and (event.scheduledDeparture between :windowStart and :windowEnd
			             or event.actualDeparture between :windowStart and :windowEnd))""")
	List<TripEntity> findDepartingFrom(
			@Param("stationId") String stationId,
			@Param("serviceDate") LocalDate serviceDate,
			@Param("windowStart") OffsetDateTime windowStart,
			@Param("windowEnd") OffsetDateTime windowEnd);
}
