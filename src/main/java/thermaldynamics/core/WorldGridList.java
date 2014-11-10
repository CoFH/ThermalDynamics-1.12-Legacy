package thermaldynamics.core;

import net.minecraft.world.World;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class WorldGridList {

    public World worldObj;

    public WorldGridList(World world) {
        this.worldObj = world;
    }

    public LinkedHashSet<MultiBlockGrid> tickingGrids = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<IMultiBlock> tickingBlocks = new LinkedHashSet<IMultiBlock>();

    public LinkedHashSet<MultiBlockGrid> gridsToRecreate = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<MultiBlockGrid> newGrids = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<MultiBlockGrid> oldGrids = new LinkedHashSet<MultiBlockGrid>();


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
            for (MultiBlockGrid grid : gridsToRecreate) {
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

        LinkedList<MultiBlockGrid> mtickinggrids = new LinkedList<MultiBlockGrid>();

        for (MultiBlockGrid grid : tickingGrids) {
            grid.tickGrid();
            if (grid.isTickProcessing()) mtickinggrids.add(grid);
        }

        if (!mtickinggrids.isEmpty()) {

            long deadline = System.nanoTime() + 100000L;
            Iterator<MultiBlockGrid> iterator = mtickinggrids.iterator();
            while (System.nanoTime() < deadline && iterator.hasNext()) {
                iterator.next().doTickProcessing(deadline);
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
