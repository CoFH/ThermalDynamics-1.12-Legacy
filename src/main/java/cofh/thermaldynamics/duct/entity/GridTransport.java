package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGridWithRoutes;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class GridTransport extends MultiBlockGridWithRoutes<DuctUnitTransportBase, GridTransport> {

	public GridTransport(World world) {

		super(world);
	}

	public GridTransport() {

		this(DimensionManager.getWorld(0));
	}

	@Override
	public void tickGrid() {

		super.tickGrid();

	}

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return aBlock instanceof DuctUnitTransportBase;
	}
}
