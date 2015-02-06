package cofh.thermaldynamics.ducts.energy.subgrid;

import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class SubTileEnergyEnder extends SubTileEnergy {

	public EnergySubGridEnder internalGrid;

	public SubTileEnergyEnder(TileMultiBlock parent) {

		super(parent);
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergySubGridEnder(parent.world());
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergySubGridEnder) newGrid;
	}

}
