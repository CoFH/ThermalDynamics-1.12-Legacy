package thermaldynamics.multiblock;

import net.minecraft.item.ItemStack;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.item.TileItemDuct;

public interface IMultiBlockRoute extends IMultiBlock {
    public abstract int getWeight();

    public abstract boolean canStuffItem();


    public boolean isOutput();

    public int getMaxRange();

    public TileMultiBlock.NeighborTypes getCachedSideType(byte side);

    public TileMultiBlock.ConnectionTypes getConnectionType(byte side);

    public IMultiBlock getCachedTile(byte side);

    TileItemDuct.routeInfo canRouteItem(ItemStack stack, boolean b, int i);

    byte getStuffedSide();
}
