package ch.usi.smb_backend.model.network;

import java.util.Objects;

/**
 * What passengers see on one operator's vehicles: IC1, S6, Bus 31. The name is not unique across
 * operators, the id is. Equal by id.
 */
public final class Line {

	private final String id;
	private final String name;
	private final String category;
	private final VehicleType vehicleType;
	private final Operator operator;

	public Line(String id, String name, String category, VehicleType vehicleType, Operator operator) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.category = Objects.requireNonNull(category, "category");
		this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType");
		this.operator = Objects.requireNonNull(operator, "operator");
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String category() {
		return category;
	}

	public VehicleType vehicleType() {
		return vehicleType;
	}

	public Operator operator() {
		return operator;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Line line && id.equals(line.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return name + " (" + operator.name() + ")";
	}
}
