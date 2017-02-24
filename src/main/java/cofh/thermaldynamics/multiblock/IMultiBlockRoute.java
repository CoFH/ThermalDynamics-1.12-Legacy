package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import net.minecraft.item.ItemStack;

public interface IMultiBlockRoute extends IMultiBlock {

	int getWeight();

	boolean canStuffItem();

	boolean isOutput();

	int getMaxRange();

	TileTDBase.NeighborTypes getCachedSideType(byte side);

	TileTDBase.ConnectionTypes getConnectionType(byte side);

	IMultiBlock getCachedTile(byte side);

	TileItemDuct.RouteInfo canRouteItem(ItemStack stack);

	byte getStuffedSide();

	boolean acceptingStuff();
}
