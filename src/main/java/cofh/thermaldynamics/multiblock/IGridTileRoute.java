package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.NeighborType;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import net.minecraft.item.ItemStack;

public interface IGridTileRoute<T extends IGridTileRoute<T, G>, G extends MultiBlockGrid<T>> extends IGridTile<T, G> {

	int getWeight();

	boolean canStuffItem();

	boolean isOutput();

	int getMaxRange();

	NeighborType getCachedSideType(byte side);

	ConnectionType getConnectionType(byte side);

	T getCachedTile(byte side);

	DuctUnitItem.RouteInfo canRouteItem(ItemStack stack);

	byte getStuffedSide();

	boolean acceptingStuff();

}
