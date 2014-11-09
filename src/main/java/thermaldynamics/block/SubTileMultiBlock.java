package thermaldynamics.block;

import cofh.lib.util.helpers.ServerHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thermaldynamics.core.TickHandler;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockFormer;
import thermaldynamics.multiblock.MultiBlockGrid;

public abstract class SubTileMultiBlock<T extends MultiBlockGrid> implements IMultiBlock {
    public T grid;
    private boolean isValid;
    public IMultiBlock parent;

    public SubTileMultiBlock(IMultiBlock parent) {
        this.parent = parent;
    }

    @Override
    public World world() {
        return parent.world();
    }


    @Override
    public void setInvalidForForming() {
        isValid = false;
    }

    @Override
    public void setValidForForming() {

        isValid = true;
    }

    @Override
    public boolean isValidForForming() {

        return isValid;
    }

    @Override
    public abstract MultiBlockGrid getNewGrid();

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        grid = (T) newGrid;
    }

    @Override
    public T getGrid() {
        return grid;
    }

    @Override
    public IMultiBlock getConnectedSide(byte side) {
        IMultiBlock connectedSide = parent.getConnectedSide(side);

        for (IMultiBlock block : connectedSide.getSubTiles()) {
            if (sameType(block))
                return block;
        }

        return null;
    }

    @Override
    public boolean isBlockedSide(int side) {
        return parent.isBlockedSide(side);
    }

    public boolean sameType(IMultiBlock other) {
        return other.getClass() == this.getClass();
    }

    @Override
    public boolean isSideConnected(byte side) {
        return parent.isSideConnected(side) && getConnectedSide(side) != null;
    }

    @Override
    public void setNotConnected(byte side) {

    }

    @Override
    public void tickMultiBlock() {

    }

    public void formGrid() {
        if (grid == null && ServerHelper.isServerWorld(parent.world())) {
            new MultiBlockFormer().formGrid(this);
        }
    }

    @Override
    public boolean tickPass(int pass) {
        return true;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean existsYet() {
        return parent.existsYet();
    }

    public void readFromNBT(NBTTagCompound tag) {
        TickHandler.addMultiBlockToCalculate(this);
    }

    public void writeToNBT(NBTTagCompound tag) {

    }


}
