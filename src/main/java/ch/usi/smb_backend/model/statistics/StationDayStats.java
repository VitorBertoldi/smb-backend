package ch.usi.smb_backend.model.statistics;

import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.time.Delay;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * How one station did on one day, computed by the data group ({@code station_day_stats}); the
 * model only carries it, so the map can colour stations without scanning every stop event.
 *
 * @param averageDepartureDelay empty when nothing departed
 * @param onTimeShare           share of departures on time, 0 to 1; empty when nothing departed
 */
public record StationDayStats(
		Station station,
		LocalDate serviceDate,
		int events,
		int cancelled,
		Optional<Delay> averageDepartureDelay,
		OptionalDouble onTimeShare) {

	public StationDayStats {
		Objects.requireNonNull(station, "station");
		Objects.requireNonNull(serviceDate, "serviceDate");
		Objects.requireNonNull(averageDepartureDelay, "averageDepartureDelay");
		Objects.requireNonNull(onTimeShare, "onTimeShare");
		if (events < 0 || cancelled < 0 || cancelled > events) {
			throw new IllegalArgumentException(
					"impossible counts at " + station + ": " + cancelled + " cancelled of " + events);
		}
		if (onTimeShare.isPresent() && (onTimeShare.getAsDouble() < 0 || onTimeShare.getAsDouble() > 1)) {
			throw new IllegalArgumentException("on-time share must be between 0 and 1, was " + onTimeShare.getAsDouble());
		}
	}
}
