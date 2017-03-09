package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.WorldGridList;
import net.minecraft.world.World;

public class GridStructural extends MultiBlockGrid {

	public GridStructural(WorldGridList worldGrid) {

		super(worldGrid);
	}

	public GridStructural(World worldObj) {

		super(worldObj);
	}

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return true;
	}
}
