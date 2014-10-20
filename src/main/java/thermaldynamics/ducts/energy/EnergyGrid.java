package thermaldynamics.ducts.energy;

import cofh.api.energy.EnergyStorage;
import net.minecraft.world.World;
import thermaldynamics.core.TDProps;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergyGrid extends MultiBlockGrid {

    public EnergyStorage myStorage = new EnergyStorage(0, TDProps.ENERGY_PER_NODE);
    private int currentEnergy = 0;
    private int extraEnergy = 0;
    private boolean first = false;

    public EnergyGrid(World world) {
        super(world);
    }

    @Override
    public void balanceGrid() {

        myStorage.setCapacity(nodeSet.size() * TDProps.ENERGY_PER_NODE);
        System.out.println("Nodes: " + nodeSet.size());
    }

    @Override
    public void tickGrid() {

        if (!nodeSet.isEmpty() && myStorage.getEnergyStored() > 0) {
            currentEnergy = myStorage.getEnergyStored() / nodeSet.size();
            extraEnergy = myStorage.getEnergyStored() % nodeSet.size();
            for (IMultiBlock m : nodeSet) {
                m.tickPass(0);
            }

        }
    }

    public int getSendableEnergy() {

        return currentEnergy + extraEnergy;
    }

    public void useEnergy(int energyUsed) {

        myStorage.modifyEnergyStored(-energyUsed);
        if (energyUsed > currentEnergy) {
            extraEnergy -= (energyUsed - currentEnergy);
            extraEnergy = Math.max(0, extraEnergy);
        }
    }
}
