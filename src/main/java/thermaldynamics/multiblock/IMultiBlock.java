package thermaldynamics.multiblock;

import net.minecraft.world.World;

public interface IMultiBlock {

    public World world();

    public int x();

    public int y();

    public int z();

    public void setInvalidForForming();

    public void setValidForForming();

    public boolean isValidForForming();

    public MultiBlockGrid getNewGrid();

    public void setGrid(MultiBlockGrid newGrid);

    public MultiBlockGrid getGrid();

    public IMultiBlock getConnectedSide(byte side);

    public boolean isBlockedSide(int side);

    public boolean isSideConnected(byte side);

    // This side contains a grid that will not form, mark that side as not connected.
    public void setNotConnected(byte side);

    // Used by some multiblocks to start their formations. Removed from the ticking list after initial tick.
    public void tickMultiBlock();

    // Used to do multiblock steps passed off by the grid. IE: Distribute liquids.
    // return false if the grid has altered
    public boolean tickPass(int pass);

    public boolean isNode();

    public boolean existsYet();

    // Some tiles will have sub-grids that may not match the parent grid
    // e.g. Ender-pipes will require power but will not share power through regular pipes
    // we could also do stuff like pipe-wire using this if we were so inclined
    public IMultiBlock[] getSubTiles();
}
