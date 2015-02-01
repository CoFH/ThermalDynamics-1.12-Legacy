package thermaldynamics.ducts.energy;

import net.minecraftforge.common.util.ForgeDirection;

import thermaldynamics.multiblock.MultiBlockGrid;

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
		} else
			return 0;
	}

}
