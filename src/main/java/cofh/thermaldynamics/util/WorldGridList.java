package cofh.thermaldynamics.util;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class WorldGridList {

	public LinkedHashSet<MultiBlockGrid> tickingGrids = new LinkedHashSet<>();
	public LinkedHashSet<IMultiBlock> tickingBlocks = new LinkedHashSet<>();

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
				for (IMultiBlock multiBlock : grid.idleSet) {
					tickingBlocks.add(multiBlock);
					grid.destroyNode(multiBlock);
				}

				for (IMultiBlock multiBlock : grid.nodeSet) {
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
			Iterator<IMultiBlock> iter = tickingBlocks.iterator();
			while (iter.hasNext()) {
				IMultiBlock block = iter.next();
				if (block.existsYet()) {
					block.tickMultiBlock();
					iter.remove();
				}
			}
		}
	}

}
