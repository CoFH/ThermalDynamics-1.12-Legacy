package thermaldynamics.multiblock;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraftforge.common.util.ForgeDirection;

public class MultiBlockFormer {

	Queue<IMultiBlock> blocksToCheck = new LinkedList<IMultiBlock>();
	MultiBlockGrid theGrid;

	public void formGrid(IMultiBlock theMultiBlock) {

		theGrid = theMultiBlock.getNewGrid();
		theMultiBlock.setGrid(theGrid);

		blocksToCheck.add(theMultiBlock);

		while (!blocksToCheck.isEmpty()) {
			checkMutliBlock(blocksToCheck.remove());
		}

		// doStep(theMultiBlock.getGrid(), theMultiBlock);

		theMultiBlock.getGrid().addBlock(theMultiBlock);
		theMultiBlock.getGrid().resetMultiBlocks();
	}

	private void checkMutliBlock(IMultiBlock currentMultiBlock) {

		if (!currentMultiBlock.isValidForForming()) {
			return;
		}

		currentMultiBlock.setInvalidForForming();

		IMultiBlock aBlock;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (currentMultiBlock.isSideConnected(i)) {
				aBlock = currentMultiBlock.getConnectedSide(i);
				if (aBlock != null && aBlock.isValidForForming()) {
					if (aBlock.getGrid() != null) {
						if (theGrid.canGridsMerge(aBlock.getGrid())) {
							if (theGrid != aBlock.getGrid()) {
								theGrid.mergeGrids(aBlock.getGrid());
							}
						} else {
							currentMultiBlock.setNotConnected(i);
						}
					} else {
						aBlock.setGrid(theGrid);
						theGrid.addBlock(aBlock);
						blocksToCheck.add(aBlock);
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
