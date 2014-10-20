package thermaldynamics.multiblock;

import thermaldynamics.block.TileMultiBlock;

public interface IMultiBlockRoute extends IMultiBlock {
    public abstract int getWeight();

    public abstract boolean canStuffItem();

    public boolean wasVisited();

    public void setVisited(boolean wasVisited);

    public boolean isOutput();

    public boolean wasOutputFound();

    public void setOutputFound(boolean outputFound);

    public int getMaxRange();

    public TileMultiBlock.NeighborTypes getCachedSideType(byte side);

    public IMultiBlock getCachedTile(byte side);
}
