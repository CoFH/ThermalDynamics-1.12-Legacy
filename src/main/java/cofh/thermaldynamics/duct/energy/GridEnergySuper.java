package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;

public class GridEnergySuper extends GridEnergy {

	int nodeTracker;
	boolean isSendingEnergy;

	DuctUnitEnergy[] nodeList = null;

	public GridEnergySuper(World world, int transferLimit, int capacity) {

		super(world, transferLimit, capacity);
		myStorage.setMaxExtract(myStorage.getMaxEnergyStored());
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		int i = 0;
		if (nodeList == null) {
			nodeList = new DuctUnitEnergy[nodeSet.size()];
			for (DuctUnitEnergy multiBlock : nodeSet) {
				nodeList[i] = multiBlock;
				i++;
			}
		}
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate) {

		if (isSendingEnergy) {
			return 0;
		}
		int tempTracker = nodeTracker;

		DuctUnitEnergy[] list = nodeList;

		if (list == null || list.length == 0) {
			return 0;
		}

		int startAmount = energy;
		isSendingEnergy = true;
		for (int i = nodeTracker; i < list.length && energy > 0; i++) {
			energy -= trackInOut(list[i].transmitEnergy(energy, simulate), simulate);
			if (energy == 0) {
				nodeTracker = i + 1;
			}
		}
		for (int i = 0; i < list.length && i < nodeTracker && energy > 0; i++) {
			energy -= trackInOut(list[i].transmitEnergy(energy, simulate), simulate);
			if (energy == 0) {
				nodeTracker = i + 1;
			}
		}
		if (energy > 0) {
			nodeTracker++;
		}
		if (nodeTracker >= list.length) {
			nodeTracker = 0;
		}
		if (simulate) {
			nodeTracker = tempTracker;
		}
		isSendingEnergy = false;
		return startAmount - energy;
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		nodeList = null;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return grid instanceof GridEnergySuper;
	}

	@Override
	public void destroy() {

		nodeList = null;
		super.destroy();
	}

}
