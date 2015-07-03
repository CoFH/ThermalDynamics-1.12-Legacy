package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.world.World;

public class EnergyGridSuper extends EnergyGrid {

	int nodeTracker;
	boolean isSendingEnergy;

	TileEnergyDuct[] nodeList = null;

	public EnergyGridSuper(World world, int type) {

		super(world, type);
		myStorage.setMaxExtract(myStorage.getMaxEnergyStored());
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		int i = 0;
		if (nodeList == null) {
			nodeList = new TileEnergyDuct[nodeSet.size()];
			for (IMultiBlock multiBlock : nodeSet) {
				nodeList[i] = (TileEnergyDuct) multiBlock;
				i++;
			}
		}
	}

	public int sendEnergy(int energy, boolean simulate) {

		if (isSendingEnergy) {
			return 0;
		}
		int tempTracker = nodeTracker;

		TileEnergyDuct[] list = nodeList;
		int startAmount = energy;

		if (list == null || list.length == 0) {
			return 0;
		}
		isSendingEnergy = true;
		for (int i = nodeTracker; i < list.length && energy > 0; i++) {
			energy -= list[i].transmitEnergy(energy, simulate);
			if (energy == 0) {
				nodeTracker = i + 1;
			}
		}
		for (int i = 0; i < list.length && i < nodeTracker && energy > 0; i++) {
			energy -= list[i].transmitEnergy(energy, simulate);
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

		return grid instanceof EnergyGridSuper;
	}

	@Override
	public void destroy() {

		nodeList = null;
		super.destroy();
	}

}
