package cofh.thermaldynamics.multiblock;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraftforge.common.util.ForgeDirection;

public class MultiBlockFormer {

	Queue<IMultiBlock> blocksToCheck = new LinkedList<IMultiBlock>();
	MultiBlockGrid theGrid;

	public void formGrid(IMultiBlock theMultiBlock) {

		theGrid = theMultiBlock.getNewGrid();
		theMultiBlock.setGrid(theGrid);
		theGrid.addBlock(theMultiBlock);

		blocksToCheck.add(theMultiBlock);

		while (!blocksToCheck.isEmpty()) {
			checkMutliBlock(blocksToCheck.remove());
		}

		// doStep(theMultiBlock.getGrid(), theMultiBlock);

		theMultiBlock.getGrid().resetMultiBlocks();
	}

	private void checkMutliBlock(IMultiBlock currentMultiBlock) {

		if (!currentMultiBlock.isValidForForming()) {
			return;
		}

		currentMultiBlock.onNeighborBlockChange();
		currentMultiBlock.setInvalidForForming();

		IMultiBlock aBlock;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (currentMultiBlock.isSideConnected(i)) {
				aBlock = currentMultiBlock.getConnectedSide(i);
				if (aBlock != null && aBlock.isValidForForming()) {
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
					} else {
						currentMultiBlock.setNotConnected(i);
						aBlock.setNotConnected((byte) (i ^ 1));
					}
				}
			}
		}
	}

	// public void doStep(MultiBlockGrid theGrid, IMultiBlock currentMultiBlock) {
	//
	// IMultiBlock aBlock;
	// for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
	// if (currentMultiBlock.isSideConnected(i)) {
	// aBlock = currentMultiBlock.getConnectedSide(i);
	// if (aBlock != null && aBlock.isValidForForming()) {
	// if (aBlock.getGrid() != null) {
	// if (theGrid.canGridsMerge(aBlock.getGrid())) {
	// if (theGrid != aBlock.getGrid()) {
	// theGrid.mergeGrids(aBlock.getGrid());
	// }
	// } else {
	// currentMultiBlock.setNotConnected(i);
	// }
	// } else {
	// aBlock.setInvalidForForming();
	// aBlock.setGrid(theGrid);
	// theGrid.addIdle(aBlock);
	// doStep(theGrid, aBlock);
	// }
	// }
	// }
	// }
	// }
}
