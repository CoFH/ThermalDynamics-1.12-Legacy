package thermaldynamics.multiblock;

import thermaldynamics.block.TileMultiBlock;

public interface IMultiBlockRoute extends IMultiBlock {
    public abstract int getWeight();

    public abstract boolean canStuffItem();


    public boolean isOutput();

    public int getMaxRange();

    public TileMultiBlock.NeighborTypes getCachedSideType(byte side);

    public TileMultiBlock.ConnectionTypes getConnectionType(byte side);

    public IMultiBlock getCachedTile(byte side);

    public int x();

    public int y();

    public int z();


}
