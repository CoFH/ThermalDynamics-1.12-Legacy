package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.item.TileItemDuct;

import net.minecraft.item.ItemStack;

public interface IMultiBlockRoute extends IMultiBlock {
    public abstract int getWeight();

    public abstract boolean canStuffItem();


    public boolean isOutput();

    public int getMaxRange();

    public TileMultiBlock.NeighborTypes getCachedSideType(byte side);

    public TileMultiBlock.ConnectionTypes getConnectionType(byte side);

    public IMultiBlock getCachedTile(byte side);

    TileItemDuct.RouteInfo canRouteItem(ItemStack stack);

    byte getStuffedSide();

    boolean acceptingStuff();
}
