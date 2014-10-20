package thermaldynamics.multiblock;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.core.TickHandler;

import java.util.HashSet;

public class MultiBlockGrid {
    public HashSet<IMultiBlock> nodeSet = new HashSet<IMultiBlock>();
    public HashSet<IMultiBlock> idleSet = new HashSet<IMultiBlock>();
    public World world;

    public MultiBlockGrid(World world) {
        this.world = world;
        TickHandler.getTickHandler(world).newGrids.add(this);
    }

    public void addIdle(IMultiBlock aMultiBlock) {
        idleSet.add(aMultiBlock);

        if (nodeSet.contains(aMultiBlock)) {
            nodeSet.remove(aMultiBlock);
            onMajorGridChange();
        } else {
            boolean flag = false;
            for (byte s = 0; s < ForgeDirection.VALID_DIRECTIONS.length; s++)
                if (aMultiBlock.isSideConnected(s)) {
                    if (flag) {
                        onMajorGridChange();
                        break;
                    } else {
                        flag = true;
                    }
                }
        }

        balanceGrid();
    }

    public void addNode(IMultiBlock aMultiBlock) {
        nodeSet.add(aMultiBlock);
        if (idleSet.contains(aMultiBlock)) {
            idleSet.remove(aMultiBlock);
        }

        onMajorGridChange();
        balanceGrid();
    }

    public void mergeGrids(MultiBlockGrid theGrid) {
        if (!nodeSet.isEmpty()) {
            for (IMultiBlock aBlock : theGrid.nodeSet) {
                aBlock.setGrid(this);

            }

            nodeSet.addAll(theGrid.nodeSet);
            onMajorGridChange();
        }

        if (theGrid.idleSet.size() == 1) {
            IMultiBlock aBlock = theGrid.idleSet.iterator().next();
            aBlock.setGrid(this);
            addIdle(aBlock);
        } else {
            for (IMultiBlock aBlock : theGrid.idleSet) {
                aBlock.setGrid(this);
            }
            idleSet.addAll(theGrid.idleSet);
            onMajorGridChange();
        }


        theGrid.destory();
    }

    public void destory() {
        nodeSet.clear();
        idleSet.clear();

        TickHandler.getTickHandler(world).oldGrids.add(this);
    }

    public boolean canGridsMerge(MultiBlockGrid grid) {
        return grid.getClass() == this.getClass();
    }

    public void resetMultiBlocks() {
        for (IMultiBlock aBlock : nodeSet) {
            aBlock.setValidForForming();
        }
        for (IMultiBlock aBlock : idleSet) {
            aBlock.setValidForForming();
        }
    }

    /*
     * Called at the end of a world tick
     */
    public void tickGrid() {

    }

    /*
     * Called whenever a set changes so that grids that rely on set sizes can rebalance.
     */
    public void balanceGrid() {

    }

    public void addBlock(IMultiBlock aBlock) {
        if (aBlock.isNode()) {
            addNode(aBlock);
        } else {
            addIdle(aBlock);
        }
    }

    public void removeBlock(IMultiBlock oldBlock) {
        if (oldBlock.isNode()) {
            nodeSet.remove(oldBlock);
            onMajorGridChange();
        } else {
            idleSet.remove(oldBlock);
        }

        byte s = 0;
        for (byte i = 0; i < 6; i++) {
            if (oldBlock.isSideConnected(i)) s++;
        }

        if (s <= 1) {
            balanceGrid();
            return;
        }

        TickHandler.getTickHandler(world).gridsToRecreate.add(this);
    }

    public void onMajorGridChange() {

    }

    public int size() {
        return nodeSet.size() + idleSet.size();
    }
}
