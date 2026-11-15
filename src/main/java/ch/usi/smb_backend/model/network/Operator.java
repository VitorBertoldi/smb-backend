package ch.usi.smb_backend.model.network;

import java.util.Objects;

/** The company running a line, e.g. {@code 11} / {@code Schweizerische Bundesbahnen SBB}. */
public record Operator(String code, String name) {

	public Operator {
		Objects.requireNonNull(code, "code");
		Objects.requireNonNull(name, "name");
	}
}
