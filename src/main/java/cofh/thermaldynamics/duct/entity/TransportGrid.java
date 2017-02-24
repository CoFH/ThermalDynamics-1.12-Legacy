package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGridWithRoutes;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class TransportGrid extends MultiBlockGridWithRoutes {

	public TransportGrid(World world) {

		super(world);
	}

	public TransportGrid() {

		this(DimensionManager.getWorld(0));
	}

	@Override
	public void tickGrid() {

		super.tickGrid();

	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof TileTransportDuctBaseRoute;
	}
}
