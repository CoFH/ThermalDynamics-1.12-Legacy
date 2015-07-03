package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraftforge.common.util.ForgeDirection;

public class TileEnergyDuctSuper extends TileEnergyDuct {

	private EnergyGridSuper internalGridSC;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGridSC = (EnergyGridSuper) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergyGridSuper(worldObj, getDuctType().type);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {

		if (this.internalGridSC != null && canConnectEnergy(from)) {
			return internalGridSC.sendEnergy(maxReceive, simulate);
		}
		return 0;
	}

}
