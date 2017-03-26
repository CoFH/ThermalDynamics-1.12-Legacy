package cofh.thermaldynamics.multiblock;

import net.minecraft.world.World;

public interface IDuctTile extends IGridTile {



	IDuctTile getConnectedSide(byte side);

	boolean isBlockedSide(int side);

	boolean isSideConnected(byte side);

	// Used by some multiblocks to start their formations. Removed from the ticking list after initial tick.
	void singleTick();

	// Used to do multiblock steps passed off by the grid. IE: Distribute liquids.
	// return false if the grid has altered
	boolean tickPass(int pass);

	boolean isNode();

	boolean existsYet();

	// Some tiles will have sub-grids that may not match the parent grid
	// e.g. Ender-pipes will require power but will not share power through regular pipes
	// we could also do stuff like pipe-wire using this if we were so inclined
	IDuctTile[] getSubTiles();

	void addRelays();

	/* PASSTHROUGH METHODS */
	void onNeighborBlockChange();

	int x();

	int y();

	int z();

	World world();

}
