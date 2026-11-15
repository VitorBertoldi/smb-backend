package ch.usi.smb_backend.service;

import java.time.LocalDate;
import java.util.List;

/** A service date the database has no trips for; carries the dates it does have. */
public class UnknownServiceDateException extends NotFoundException {

	private final List<LocalDate> availableDates;

	public UnknownServiceDateException(LocalDate date, List<LocalDate> availableDates) {
		super("no trips on service date " + date);
		this.availableDates = List.copyOf(availableDates);
	}

	public List<LocalDate> availableDates() {
		return availableDates;
	}
}
