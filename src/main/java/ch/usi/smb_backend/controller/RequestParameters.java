package ch.usi.smb_backend.controller;

import ch.usi.smb_backend.model.time.ServiceDay;
import ch.usi.smb_backend.model.time.ServiceTime;
import java.time.Duration;

/** Turns request parameters that need a service day, or each other, into model values. */
final class RequestParameters {

	private RequestParameters() {
	}

	/**
	 * @throws BadRequestException when the text is not a clock time
	 */
	static ServiceTime clock(ServiceDay day, String parameter, String text) {
		try {
			return day.parseClock(text);
		} catch (IllegalArgumentException notAClockTime) {
			throw new BadRequestException("'" + parameter + "': " + notAClockTime.getMessage());
		}
	}

	/**
	 * @throws BadRequestException when the window ends before it starts
	 */
	static void requireOrdered(ServiceTime from, ServiceTime to) {
		if (to.isBefore(from)) {
			throw new BadRequestException("'from' must not be after 'to'");
		}
	}

	/**
	 * @throws BadRequestException when the window is longer than {@code limit}
	 */
	static void requireAtMost(ServiceTime from, ServiceTime to, Duration limit) {
		if (from.secondsUntil(to) > limit.toSeconds()) {
			throw new BadRequestException("the window may be at most " + limit.toHours() + " hours long");
		}
	}

	/**
	 * @throws BadRequestException when the text is blank
	 */
	static String requireText(String parameter, String text) {
		if (text.isBlank()) {
			throw new BadRequestException("'" + parameter + "' must not be blank");
		}
		return text.strip();
	}
}
