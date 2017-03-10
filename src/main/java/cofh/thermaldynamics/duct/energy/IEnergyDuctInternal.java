package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IMultiBlock;

public interface IEnergyDuctInternal extends IMultiBlock {

	int getEnergyForGrid();

	void setEnergyForGrid(int energy);

	int transmitEnergy(int energy, boolean simulate);
}
