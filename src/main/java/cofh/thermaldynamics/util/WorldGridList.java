package cofh.thermaldynamics.util;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.IOccasionalTick;
import cofh.thermaldynamics.multiblock.ISingleTick;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class WorldGridList {

	public LinkedHashSet<MultiBlockGrid> tickingGrids = new LinkedHashSet<>();
	public LinkedHashSet<ISingleTick> tickingBlocks = new LinkedHashSet<>();
	public ArrayList<LinkedHashSet<IOccasionalTick>> occasionalTickingBlocks = new ArrayList<>();

	public LinkedHashSet<MultiBlockGrid> gridsToRecreate = new LinkedHashSet<>();
	public LinkedHashSet<MultiBlockGrid> newGrids = new LinkedHashSet<>();
	public LinkedHashSet<MultiBlockGrid> oldGrids = new LinkedHashSet<>();

	public World worldObj;

	public WorldGridList(World world) {

		this.worldObj = world;
	}

	public void tickStart() {

		if (!newGrids.isEmpty()) {
			tickingGrids.addAll(newGrids);
			newGrids.clear();
		}
		if (!oldGrids.isEmpty()) {
			tickingGrids.removeAll(oldGrids);
			oldGrids.clear();
		}
	}

	public void tickEnd() {

		if (!gridsToRecreate.isEmpty()) {
			tickingGrids.removeAll(gridsToRecreate);
			for (MultiBlockGrid<?> grid : gridsToRecreate) {
				for (IGridTile multiBlock : grid.idleSet) {
					tickingBlocks.add(multiBlock);
					grid.destroyNode(multiBlock);
				}

				for (IGridTile multiBlock : grid.nodeSet) {
					tickingBlocks.add(multiBlock);
					grid.destroyNode(multiBlock);
				}
			}
			gridsToRecreate.clear();
		}
		ArrayList<MultiBlockGrid> mtickinggrids = new ArrayList<>();

		for (MultiBlockGrid grid : tickingGrids) {
			grid.tickGrid();
			if (grid.isTickProcessing()) {
				mtickinggrids.add(grid);
			}
		}
		if (!mtickinggrids.isEmpty()) {
			long deadline = System.nanoTime() + 100000L;
			for (int i = 0, e = mtickinggrids.size(), c = 0; i < e; ++i) {
				mtickinggrids.get(i).doTickProcessing(deadline);
				if (c++ == 7) {
					if (System.nanoTime() > deadline) {
						break;
					}
					c = 0;
				}
			}
		}
		if (!tickingBlocks.isEmpty()) {
			Iterator<ISingleTick> iter = tickingBlocks.iterator();
			while (iter.hasNext()) {
				ISingleTick block = iter.next();
				if (block.existsYet()) {
					block.singleTick();
					iter.remove();
				} else if (block.isOutdated()) {
					iter.remove();
				}
			}
		}

		int lastNonEmptyList = -1;
		for (int i = 0; i < occasionalTickingBlocks.size(); i++) {
			LinkedHashSet<IOccasionalTick> list = occasionalTickingBlocks.get(i);
			for (Iterator<IOccasionalTick> iterator = list.iterator(); iterator.hasNext(); ) {
				IOccasionalTick iOccasionalTick = iterator.next();
				if (!iOccasionalTick.occasionalTick(i)) {
					iterator.remove();
				}
			}
			if (!list.isEmpty()) {
				lastNonEmptyList = i;
			}
		}
		if (lastNonEmptyList == -1) {
			occasionalTickingBlocks.clear();
		} else if ((lastNonEmptyList + 1) < occasionalTickingBlocks.size()) {
			for (int i = occasionalTickingBlocks.size() - 1; i >= lastNonEmptyList + 1; i--) {
				occasionalTickingBlocks.remove(i);
			}
		}
	}


}
