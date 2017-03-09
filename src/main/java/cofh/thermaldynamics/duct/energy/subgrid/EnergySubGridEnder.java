package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import com.google.common.collect.Iterables;
import net.minecraft.world.World;

public class EnergySubGridEnder extends EnergySubGrid<SubTileEnergyEnder> {

	public EnergySubGridEnder(World world) {

		super(world, 16 * TDProps.ENDER_TRANSMIT_COST, 16 * TDProps.ENDER_TRANSMIT_COST);
	}

	public boolean isPowered() {

		return myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST;
	}

	@Override
	public void tickGrid() {

		if (updateRenderer) {
			for (SubTileEnergyEnder iMultiBlock : Iterables.concat(nodeSet, idleSet)) {
				iMultiBlock.parentTile.updateRender();
			}
		}
	}

	@Override
	public void addBlock(SubTileEnergyEnder aBlock) {

		super.addBlock(aBlock);
		updateRenderer = true;
	}

	boolean updateRenderer = false;

	@Override
	public void mergeGrids(MultiBlockGrid<SubTileEnergyEnder> theGrid) {

		super.mergeGrids(theGrid);
		updateRenderer = true;
	}
}
