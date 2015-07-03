package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.core.WorldGridList;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.world.World;

public class GridStructural extends MultiBlockGrid {

	public GridStructural(WorldGridList worldGrid) {

		super(worldGrid);
	}

	public GridStructural(World worldObj) {

		super(worldObj);
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return true;
	}
}
