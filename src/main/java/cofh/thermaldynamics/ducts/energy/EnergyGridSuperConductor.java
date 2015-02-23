package cofh.thermaldynamics.ducts.energy;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.world.World;

public class EnergyGridSuperConductor extends EnergyGrid {

	int nodeTracker;

	public EnergyGridSuperConductor(World world, int type) {

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
			overSent = new boolean[nodeList.length];
		}
	}

	TileEnergyDuct[] nodeList = null;

	boolean[] overSent = null;

	public int sendEnergy(int energy, boolean simulate) {

		TileEnergyDuct[] list = nodeList;
		int startAmount = energy;

		if (list == null || list.length == 0) {
			return myStorage.receiveEnergy(energy, simulate);
		}
		for (int i = nodeTracker; i < list.length && energy > 0; i++) {
			energy -= list[i].transmitEnergy(energy);
		}
		for (int i = 0; i < list.length && i < nodeTracker && energy > 0; i++) {
			energy -= list[i].transmitEnergy(energy);
		}
		nodeTracker++;
		if (nodeTracker >= list.length) {
			nodeTracker = 0;
		}
		return startAmount - energy;
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		nodeList = null;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return grid instanceof EnergyGridSuperConductor;
	}

	@Override
	public void destroy() {

		nodeList = null;
		super.destroy();
	}

}
