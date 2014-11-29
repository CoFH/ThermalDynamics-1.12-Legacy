package thermaldynamics.ducts.energy.subgrid;

import cofh.api.energy.EnergyStorage;
import net.minecraft.world.World;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergySubGrid extends MultiBlockGrid {
    public EnergyStorage myStorage = new EnergyStorage(1000);
    public final int perStorage ;

    public EnergySubGrid(World worldObj, int perStorage, int maxTransfer) {
        super(worldObj);
        this.perStorage = perStorage;
        myStorage.setMaxTransfer(maxTransfer);
    }

    @Override
    public void balanceGrid() {
        myStorage.setCapacity(nodeSet.size() * perStorage);
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {
        return aBlock instanceof SubTileEnergy;
    }

    @Override
    public boolean canGridsMerge(MultiBlockGrid grid) {
        return super.canGridsMerge(grid);
    }

    public int getNodeShare(IMultiBlock conduitEnergy) {
        return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(conduitEnergy) ? myStorage.getEnergyStored() / nodeSet.size()
                + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
    }

    @Override
    public void addBlock(IMultiBlock aBlock) {
        super.addBlock(aBlock);
        SubTileEnergy theCondE = (SubTileEnergy) aBlock;
        if (theCondE.energyForGrid > 0) {
            myStorage.modifyEnergyStored(theCondE.energyForGrid);
        }
    }

    @Override
    public void mergeGrids(MultiBlockGrid theGrid) {
        super.mergeGrids(theGrid);
        balanceGrid();
        myStorage.modifyEnergyStored(((EnergySubGrid) theGrid).myStorage.getEnergyStored());
    }

    @Override
    public void destroyNode(IMultiBlock node) {
        if (node.isNode()) ((SubTileEnergy) node).energyForGrid = getNodeShare(node);
        super.destroyNode(node);
    }
}
