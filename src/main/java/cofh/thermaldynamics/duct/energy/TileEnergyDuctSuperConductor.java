package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraftforge.common.util.ForgeDirection;

public class TileEnergyDuctSuperConductor extends TileEnergyDuct {

	private EnergyGridSuperConductor internalGridSC;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGridSC = (EnergyGridSuperConductor) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergyGridSuperConductor(worldObj, getDuctType().type);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {

		if (this.internalGridSC != null && canConnectEnergy(from)) {
			return internalGridSC.sendEnergy(maxReceive, simulate);
		} else {
			return 0;
		}
	}

}
