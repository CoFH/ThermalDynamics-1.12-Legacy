package thermaldynamics.ducts.energy;

import cofh.api.energy.EnergyStorage;
import net.minecraft.world.World;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergyGrid extends MultiBlockGrid {

    public final EnergyStorage myStorage;
    private int currentEnergy = 0;
    private int extraEnergy = 0;

    private int type;

    public static final int NODE_STORAGE[] = {480, 2400, 60000, 60000};
    public static final int NODE_TRANSFER[] = {80, 400, 10000, 10000};

    public EnergyGrid(World world, int type) {
        super(world);
        this.type = type;
        myStorage = new EnergyStorage(NODE_STORAGE[type], NODE_TRANSFER[type]);
    }

    @Override
    public void balanceGrid() {
        myStorage.setCapacity(nodeSet.size() * NODE_STORAGE[type]);
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {
        return aBlock instanceof TileEnergyDuct && ((TileEnergyDuct) aBlock).getDuctType().type == this.type;
    }

    @Override
    public void tickGrid() {
        if (!nodeSet.isEmpty() && myStorage.getEnergyStored() > 0) {
            currentEnergy = myStorage.getEnergyStored() / nodeSet.size();
            extraEnergy = myStorage.getEnergyStored() % nodeSet.size();
            for (IMultiBlock m : nodeSet) {
                if (!m.tickPass(0))
                    break;
            }

        }
    }

    public int getSendableEnergy() {
        return Math.min(myStorage.getMaxExtract(), currentEnergy + extraEnergy);
    }

    public void useEnergy(int energyUsed) {
        myStorage.modifyEnergyStored(-energyUsed);
        if (energyUsed > currentEnergy) {
            extraEnergy -= (energyUsed - currentEnergy);
            extraEnergy = Math.max(0, extraEnergy);
        }
    }

    @Override
    public boolean canGridsMerge(MultiBlockGrid grid) {
        return super.canGridsMerge(grid) && ((EnergyGrid) grid).type == this.type;
    }

    @Override
    public void addNode(IMultiBlock aMultiBlock) {
        super.addNode(aMultiBlock);

        TileEnergyDuct theCondE = (TileEnergyDuct) aMultiBlock;
        if (theCondE.energyForGrid > 0) {
            myStorage.modifyEnergyStored(theCondE.energyForGrid);
        }
    }

    @Override
    public void removeBlock(IMultiBlock oldBlock) {
        if (oldBlock.isNode()) ((TileEnergyDuct) oldBlock).energyForGrid = getNodeShare(oldBlock);
        super.removeBlock(oldBlock);
    }

    public int getNodeShare(IMultiBlock conduitEnergy) {
        return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(conduitEnergy) ? myStorage.getEnergyStored() / nodeSet.size()
                + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
    }


}
