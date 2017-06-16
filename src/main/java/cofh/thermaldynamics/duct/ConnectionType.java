package cofh.thermaldynamics.duct;

public enum ConnectionType {

	NORMAL(true), BLOCKED(false), FORCED(true);

	ConnectionType(boolean allowTransfer) {

		this.allowTransfer = allowTransfer;
	}

	public final boolean allowTransfer;

	public ConnectionType next() {

		if (this == NORMAL) {
			return BLOCKED;
		}
		return NORMAL;
	}

}
