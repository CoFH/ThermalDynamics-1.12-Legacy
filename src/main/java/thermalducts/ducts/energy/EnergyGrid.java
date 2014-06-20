package thermalducts.ducts.energy;

import cofh.api.energy.EnergyStorage;

import thermalducts.core.TDProps;
import thermalducts.multiblock.IMultiBlock;
import thermalducts.multiblock.MultiBlockGrid;

public class EnergyGrid extends MultiBlockGrid {

	public EnergyStorage myStorage = new EnergyStorage(0);
	private int currentEnergy = 0;

	@Override
	public void balanceGrid() {

		myStorage.setCapacity(nodeSet.size() * TDProps.ENERGY_PER_NODE);
		System.out.println("Nodes: " + nodeSet.size());
	}

	@Override
	public void tickGrid() {

		if (!nodeSet.isEmpty())
			synchronized (nodeSet) {
				currentEnergy = myStorage.getEnergyStored() % nodeSet.size();
				for (IMultiBlock m : nodeSet) {
					m.tickPass(0);
				}
			}
	}

	public int getSendableEnergy() {

		return currentEnergy;
	}
}
