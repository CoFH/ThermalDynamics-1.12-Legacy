package cofh.thermaldynamics.duct;

public enum ConnectionType {

	NORMAL(true, true), BLOCKED(false, false), ENERGY(false, true), FORCED(true, true);

	public static final ConnectionType[] VALUES = values();

	ConnectionType(boolean allowTransfer, boolean allowEnergy) {

		this.allowTransfer = allowTransfer;
		this.allowEnergy = allowEnergy;
	}

	public final boolean allowTransfer;
	public final boolean allowEnergy;

	public ConnectionType next() {

		if (this == NORMAL) {
			return BLOCKED;
		}
		return NORMAL;
	}

}
