package thermalducts.multiblock;

import java.util.HashSet;

public class MultiBlockGrid {

	public HashSet<IMultiBlock> nodeSet = new HashSet<IMultiBlock>();
	public HashSet<IMultiBlock> idleSet = new HashSet<IMultiBlock>();

	public void addIdle(IMultiBlock aMultiBlock) {

		idleSet.add(aMultiBlock);
		if (nodeSet.contains(aMultiBlock)) {
			nodeSet.remove(aMultiBlock);
		}
	}

	public void mergeGrids(MultiBlockGrid theGrid) {

		for (IMultiBlock aBlock : theGrid.nodeSet) {
			aBlock.setGrid(this);
		}
		for (IMultiBlock aBlock : theGrid.idleSet) {
			aBlock.setGrid(this);
		}
		nodeSet.addAll(theGrid.nodeSet);
		idleSet.addAll(theGrid.idleSet);
		theGrid.destory();
	}

	public void destory() {

		nodeSet.clear();
		idleSet.clear();
	}

	public boolean canGridsMerge(MultiBlockGrid grid) {

		return true;
	}

	public void resetMultiBlocks() {

		for (IMultiBlock aBlock : nodeSet) {
			aBlock.setValidForForming();
		}
		for (IMultiBlock aBlock : idleSet) {
			aBlock.setValidForForming();
		}
	}
}
