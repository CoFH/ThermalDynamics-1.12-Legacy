package thermaldynamics.ducts.fluid;

import cofh.api.energy.EnergyStorage;
import cofh.lib.util.helpers.FluidHelper;
import net.minecraft.world.World;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.multiblock.IMultiBlock;

public class FluidEnergyGrid extends FluidGrid {
    private int CONDUIT_DRAIN = 10;
    private int CONDUIT_STORAGE = 100;

    public EnergyStorage myStorage;

    public FluidEnergyGrid(World world, int type) {
        super(world, type);
        myStorage = new EnergyStorage(CONDUIT_DRAIN);
    }

    @Override
    public void tickGrid() {
        super.tickGrid();
    }

    @Override
    public void balanceGrid() {
        super.balanceGrid();
        myStorage.setCapacity(CONDUIT_DRAIN * size());
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {
        return aBlock instanceof TileFluidDuctPowered && ((TileFluidDuct) aBlock).getDuctType().type == type
                && FluidHelper.isFluidEqualOrNull(((TileFluidDuct) aBlock).getConnectionFluid(), myTank.getFluid());
    }


    @Override
    public void addNode(IMultiBlock aMultiBlock) {
        super.addNode(aMultiBlock);

        TileFluidDuctPowered theCondE = (TileFluidDuctPowered) aMultiBlock;
        if (theCondE.energyForGrid > 0) {
            myStorage.modifyEnergyStored(theCondE.energyForGrid);
        }
    }


    @Override
    public void removeBlock(IMultiBlock oldBlock) {
        if (oldBlock.isNode()) ((TileFluidDuctPowered) oldBlock).energyForGrid = getNodeShare(oldBlock);
        super.removeBlock(oldBlock);
    }

    public int getNodeShare(IMultiBlock conduitEnergy) {
        return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(conduitEnergy) ? myStorage.getEnergyStored() / nodeSet.size()
                + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
    }

}
