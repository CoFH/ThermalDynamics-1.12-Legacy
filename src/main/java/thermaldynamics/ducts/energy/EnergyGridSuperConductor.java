package thermaldynamics.ducts.energy;

import cofh.lib.util.helpers.MathHelper;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergyGridSuperConductor extends EnergyGrid {

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

	public int sendEnergy(int maxSent, boolean simulate) {

		TileEnergyDuct[] list = nodeList;
		if (list == null || list.length == 0 || maxSent <= 0)
			return myStorage.receiveEnergy(maxSent, simulate);

		int curSent = 0;
		int toDistribute = maxSent / list.length;

		for (int i = 0; i < list.length && curSent < maxSent; i++) {
			int t = sendEnergytoTile(list[i], 0, maxSent - curSent, toDistribute);
			overSent[i] = t >= toDistribute && toDistribute < maxSent;
			curSent += t;
		}

		for (int i = 0; i < list.length; i++) {
			if (overSent[i] && curSent < maxSent) {
				curSent = sendEnergytoTile(list[i], curSent, maxSent, maxSent - toDistribute);
			}

			if (!simulate && i > 0) {
				int j = MathHelper.RANDOM.nextInt(i + 1);
				if (i != j) {
					TileEnergyDuct t = list[i];
					list[i] = list[j];
					list[j] = t;
				}
			}
		}

		curSent += myStorage.receiveEnergy(maxSent - curSent, simulate);

		return curSent;
	}

	public int sendEnergytoTile(TileEnergyDuct dest, int curSent, int maxSent, int toDistribute) {

		for (int i = 0; i < 6 && curSent < maxSent; i++) {
			if (dest.neighborTypes[i] == TileMultiBlock.NeighborTypes.OUTPUT) {
				if (dest.cache[i] != null) {
					curSent += dest.cache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], Math.min(toDistribute, maxSent - curSent), false);
				}
			}
		}
		return curSent;
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
