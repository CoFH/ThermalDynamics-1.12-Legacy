package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class SubTileEnergyEnder extends SubTileEnergy {

	public EnergySubGridEnder internalGrid;
	public TileItemDuctEnder parentTile;

	public SubTileEnergyEnder(TileItemDuctEnder parent) {

		super(parent);
		parentTile = parent;
	}

	@Override
	public MultiBlockGrid createGrid() {

		return new EnergySubGridEnder(parent.world());
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergySubGridEnder) newGrid;
	}

}
