package thermaldynamics.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.LinkedHashSet;

import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TickHandler {

	public static TickHandler INSTANCE = new TickHandler();

	public LinkedHashSet<MultiBlockGrid> tickingGrids = new LinkedHashSet<MultiBlockGrid>();
	public LinkedHashSet<IMultiBlock> tickingBlocks = new LinkedHashSet<IMultiBlock>();

	public LinkedHashSet<IMultiBlock> multiBlocksToCalculate = new LinkedHashSet<IMultiBlock>();
	public LinkedHashSet<MultiBlockGrid> newGrids = new LinkedHashSet<MultiBlockGrid>();
	public LinkedHashSet<MultiBlockGrid> oldGrids = new LinkedHashSet<MultiBlockGrid>();

	@SubscribeEvent
	public void tick(ServerTickEvent evt) {

		// TODO: this needs split up into groups per-world when worlds are threaded
		if (evt.phase == Phase.START) {
			tickStart();
		} else {
			tickEnd();
		}
	}

	public void tickStart() {

		if (!oldGrids.isEmpty()) {
			synchronized (oldGrids) {
				tickingGrids.removeAll(oldGrids);
				oldGrids.clear();
			}
		}
		if (!newGrids.isEmpty()) {
			synchronized (newGrids) {
				tickingGrids.addAll(newGrids);
				newGrids.clear();
			}
		}

		if (!multiBlocksToCalculate.isEmpty()) {
			synchronized (multiBlocksToCalculate) {
				tickingBlocks.addAll(multiBlocksToCalculate);
				multiBlocksToCalculate.clear();
			}
		}

	}

	public void tickEnd() {

		for (MultiBlockGrid grid : tickingGrids) {
			grid.tickGrid();
		}
		if (!tickingBlocks.isEmpty()) {
			for (IMultiBlock block : tickingBlocks) {
				block.tickMultiBlock();
			}
			tickingBlocks.clear();
		}
	}

}
