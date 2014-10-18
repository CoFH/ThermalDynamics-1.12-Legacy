package thermaldynamics.core;

import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

import java.util.LinkedHashSet;

public class WorldGridList {
    public LinkedHashSet<MultiBlockGrid> tickingGrids = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<IMultiBlock> tickingBlocks = new LinkedHashSet<IMultiBlock>();

    public LinkedHashSet<MultiBlockGrid> gridsToRecreate = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<MultiBlockGrid> newGrids = new LinkedHashSet<MultiBlockGrid>();
    public LinkedHashSet<MultiBlockGrid> oldGrids = new LinkedHashSet<MultiBlockGrid>();


    public void tickStart() {
        if (!oldGrids.isEmpty()) {
            tickingGrids.removeAll(oldGrids);
            oldGrids.clear();
        }

        if (!newGrids.isEmpty()) {
            tickingGrids.addAll(newGrids);
            newGrids.clear();
        }
    }

    public void tickEnd() {
        if (!gridsToRecreate.isEmpty()) {
            tickingGrids.removeAll(gridsToRecreate);
            for (MultiBlockGrid grid : gridsToRecreate) {
                for (IMultiBlock multiBlock : grid.idleSet) {
                    tickingBlocks.add(multiBlock);
                    multiBlock.setGrid(null);
                }
                
                for (IMultiBlock multiBlock : grid.nodeSet) {
                    tickingBlocks.add(multiBlock);
                    multiBlock.setGrid(null);
                }
            }
            gridsToRecreate.clear();
        }

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
