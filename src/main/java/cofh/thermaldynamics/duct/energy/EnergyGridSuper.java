package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;

public class EnergyGridSuper<T extends IEnergyDuctInternal> extends EnergyGrid<T> {

	int nodeTracker;
	boolean isSendingEnergy;

	IEnergyDuctInternal[] nodeList = null;

	public EnergyGridSuper(World world, int type) {

		super(world, type);
		myStorage.setMaxExtract(myStorage.getMaxEnergyStored());
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		int i = 0;
		if (nodeList == null) {
			nodeList = new IEnergyDuctInternal[nodeSet.size()];
			for (IEnergyDuctInternal multiBlock : nodeSet) {
				nodeList[i] = multiBlock;
				i++;
			}
		}
	}

	public int sendEnergy(int energy, boolean simulate) {

		if (isSendingEnergy) {
			return 0;
		}
		int tempTracker = nodeTracker;

		IEnergyDuctInternal[] list = nodeList;
		int startAmount = energy;

		if (list == null || list.length == 0) {
			return 0;
		}
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

		return grid instanceof EnergyGridSuper;
	}

	@Override
	public void destroy() {

		nodeList = null;
		super.destroy();
	}

}
