package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import net.minecraft.item.ItemStack;

public interface IGridTileRoute extends IGridTile {

	int getWeight();

	boolean canStuffItem();

	boolean isOutput();

	int getMaxRange();

	TileDuctBase.NeighborTypes getCachedSideType(byte side);

	TileDuctBase.ConnectionTypes getConnectionType(byte side);

	IGridTile getCachedTile(byte side);

	TileItemDuct.RouteInfo canRouteItem(ItemStack stack);

	byte getStuffedSide();

	boolean acceptingStuff();

}
