package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.duct.nutypeducts.DuctCache;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import net.minecraft.util.EnumFacing;

import java.util.LinkedList;
import java.util.Queue;

public class MultiBlockFormer2<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C extends DuctCache> {


	Queue<T> blocksToCheck = new LinkedList<>();
	G theGrid;

	public void formGrid(T theMultiBlock) {

		theGrid = theMultiBlock.createGrid();
		theMultiBlock.setGrid(theGrid);
		theGrid.addBlock(theMultiBlock);

		blocksToCheck.add(theMultiBlock);

		while (!blocksToCheck.isEmpty()) {
			checkMultiBlock(blocksToCheck.remove());
		}
		theGrid.resetMultiBlocks();
	}

	private void checkMultiBlock(T currentMultiBlock) {

		if (!currentMultiBlock.isValidForForming()) {
			return;
		}
		currentMultiBlock.onNeighborBlockChange();
		currentMultiBlock.setInvalidForForming();

		T aBlock;
		for (byte i = 0; i < EnumFacing.VALUES.length; i++) {
			if (currentMultiBlock.isSideConnected(i)) {
				aBlock = currentMultiBlock.getConnectedSide(i);
				if (aBlock != null && aBlock.isValidForForming() && aBlock.getConnectedSide(i ^ 1) == currentMultiBlock) {
					if (aBlock.getGrid() == null && theGrid.canAddBlock(aBlock)) {
						aBlock.setGrid(theGrid);
						theGrid.addBlock(aBlock);
						blocksToCheck.add(aBlock);
					} else if (theGrid.canAddBlock(aBlock) && aBlock.getGrid() != null && theGrid.canGridsMerge(aBlock.getGrid())) {
						if (theGrid != aBlock.getGrid()) {
							if (theGrid.size() >= aBlock.getGrid().size()) {
								theGrid.mergeGrids(aBlock.getGrid());
							} else {
								aBlock.getGrid().mergeGrids(theGrid);
								theGrid = aBlock.getGrid();
							}
						}
					} else{
						currentMultiBlock.onConnectionRejected(i);
						aBlock.onConnectionRejected(i ^ 1);
					}
				}
			}
		}
	}

}
