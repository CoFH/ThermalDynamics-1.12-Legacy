package thermalducts.multiblock;

import java.util.HashSet;

import thermalducts.core.TickHandler;

public class MultiBlockGrid {

	public HashSet<IMultiBlock> nodeSet = new HashSet<IMultiBlock>();
	public HashSet<IMultiBlock> idleSet = new HashSet<IMultiBlock>();

	public MultiBlockGrid() {

		synchronized (TickHandler.INSTANCE.newGrids) {
			TickHandler.INSTANCE.newGrids.add(this);
		}
	}

	public void addIdle(IMultiBlock aMultiBlock) {

		synchronized (idleSet) {
			idleSet.add(aMultiBlock);
		}
		synchronized (nodeSet) {
			if (nodeSet.contains(aMultiBlock)) {
				nodeSet.remove(aMultiBlock);
			}
		}

		balanceGrid();
	}

	public void addNode(IMultiBlock aMultiBlock) {

		synchronized (nodeSet) {
			nodeSet.add(aMultiBlock);
		}
		synchronized (idleSet) {
			if (idleSet.contains(aMultiBlock)) {
				idleSet.remove(aMultiBlock);
			}
		}

		balanceGrid();
	}

	public void mergeGrids(MultiBlockGrid theGrid) {

		synchronized (nodeSet) {
			for (IMultiBlock aBlock : theGrid.nodeSet) {
				aBlock.setGrid(this);
			}
			nodeSet.addAll(theGrid.nodeSet);
		}
		synchronized (idleSet) {
			for (IMultiBlock aBlock : theGrid.idleSet) {
				aBlock.setGrid(this);
			}
			idleSet.addAll(theGrid.idleSet);
		}
		theGrid.destory();
	}

	public void destory() {

		synchronized (nodeSet) {
			synchronized (idleSet) {
				nodeSet.clear();
				idleSet.clear();
			}
		}

		synchronized (TickHandler.INSTANCE.oldGrids) {
			TickHandler.INSTANCE.oldGrids.add(this);
		}
	}

	public boolean canGridsMerge(MultiBlockGrid grid) {

		return true;
	}

	public void resetMultiBlocks() {

		synchronized (nodeSet) {
			for (IMultiBlock aBlock : nodeSet) {
				aBlock.setValidForForming();
			}
		}
		synchronized (idleSet) {
			for (IMultiBlock aBlock : idleSet) {
				aBlock.setValidForForming();
			}
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
}
