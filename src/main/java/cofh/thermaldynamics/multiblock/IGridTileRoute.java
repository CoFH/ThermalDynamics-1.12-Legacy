package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.duct.ConnectionType;

public interface IGridTileRoute<T extends IGridTileRoute<T, G>, G extends MultiBlockGrid<T>> extends IGridTile<T, G> {

	int getWeight();

	boolean canStuffItem();

	boolean isOutput();

	int getMaxRange();

	ConnectionType getConnectionType(byte side);

	T getCachedTile(byte side);

	byte getStuffedSide();

	boolean acceptingStuff();

}
