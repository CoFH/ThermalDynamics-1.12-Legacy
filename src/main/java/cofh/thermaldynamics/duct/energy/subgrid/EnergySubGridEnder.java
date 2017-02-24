package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import com.google.common.collect.Iterables;
import net.minecraft.world.World;

public class EnergySubGridEnder extends EnergySubGrid {

	public EnergySubGridEnder(World world) {

		super(world, 16 * TDProps.ENDER_TRANSMIT_COST, 16 * TDProps.ENDER_TRANSMIT_COST);
	}

	public boolean isPowered() {

		return myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST;
	}

	@Override
	public void tickGrid() {

		if (updateRenderer) {
			for (Object iMultiBlock : Iterables.concat(nodeSet, idleSet)) {
				((SubTileEnergyEnder) iMultiBlock).parentTile.updateRender();
			}
		}
	}

	@Override
	public void addBlock(IMultiBlock aBlock) {

		super.addBlock(aBlock);
		updateRenderer = true;
	}

	boolean updateRenderer = false;

	@Override
	public void mergeGrids(MultiBlockGrid theGrid) {

		super.mergeGrids(theGrid);
		updateRenderer = true;
	}
}
