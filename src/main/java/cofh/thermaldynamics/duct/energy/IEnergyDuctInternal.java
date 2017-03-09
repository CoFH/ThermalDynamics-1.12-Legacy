package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IGridTile;

public interface IEnergyDuctInternal extends IGridTile {

	int getEnergyForGrid();

	void setEnergyForGrid(int energy);

	int transmitEnergy(int energy, boolean simulate);
}
