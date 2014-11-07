package thermaldynamics.ducts;

import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileStructuralDuct extends TileMultiBlock {
    @Override
    public void tickMultiBlock() {
        onNeighborBlockChange();
    }

    @Override
    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile != null && theTile.getClass() == this.getClass()
                && theTile.getBlockType() == this.getBlockType()
                && theTile.getBlockMetadata() == this.getBlockMetadata();
    }

    @Override
    public void formGrid() {
        // No Grids needed
    }

    @Override
    public MultiBlockGrid getNewGrid() {
        return null;
    }
}
