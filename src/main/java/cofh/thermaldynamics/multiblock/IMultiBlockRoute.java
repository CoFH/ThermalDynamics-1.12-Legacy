package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;

import net.minecraft.item.ItemStack;

public interface IMultiBlockRoute extends IMultiBlock {

	public abstract int getWeight();

	public abstract boolean canStuffItem();

	public boolean isOutput();

	public int getMaxRange();

	public TileTDBase.NeighborTypes getCachedSideType(byte side);

	public TileTDBase.ConnectionTypes getConnectionType(byte side);

	public IMultiBlock getCachedTile(byte side);

	TileItemDuct.RouteInfo canRouteItem(ItemStack stack);

	byte getStuffedSide();

	boolean acceptingStuff();
}
