package cofh.thermaldynamics.ducts.energy.subgrid;

import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class SubTileEnergyRedstone extends SubTileEnergy {

	public static int NODE_TRANSFER = 2000;
	public static int NODE_STORAGE = 12000;

	public EnergySubGridDistribute internalGrid;

	public SubTileEnergyRedstone(TileMultiBlock parent) {

		super(parent);
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergySubGridDistribute(parent.world(), NODE_STORAGE, NODE_TRANSFER);
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergySubGridDistribute) newGrid;
	}

}
