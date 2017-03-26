package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public interface IEnergyDuctInternal<T extends IEnergyDuctInternal<T, G>, G extends EnergyGrid<T>> extends IGridTile<T, G> {

	int getEnergyForGrid();

	void setEnergyForGrid(int energy);

	int transmitEnergy(int energy, boolean simulate);
}
