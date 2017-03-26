package cofh.thermaldynamics.duct;

public enum NeighborType {
	NONE, MULTIBLOCK, OUTPUT(true), INPUT(true), STRUCTURE(true), DUCT_ATTACHMENT;

	NeighborType() {

		this(false);
	}

	// Are we attached to a non-multiblock tile
	public final boolean attachedToNeightbour;

	NeighborType(boolean b) {

		this.attachedToNeightbour = b;
	}
}
