package cofh.thermaldynamics.multiblock;

import net.minecraft.util.EnumFacing;

import java.util.LinkedList;
import java.util.Queue;

public class MultiBlockFormer {

	Queue<IGridTile> blocksToCheck = new LinkedList<>();
	MultiBlockGrid theGrid;

	public void formGrid(IGridTile theMultiBlock) {

		theGrid = theMultiBlock.createGrid();
		theMultiBlock.setGrid(theGrid);
		theGrid.addBlock(theMultiBlock);

		blocksToCheck.add(theMultiBlock);

		while (!blocksToCheck.isEmpty()) {
			checkMultiBlock(blocksToCheck.remove());
		}
		theMultiBlock.getGrid().resetMultiBlocks();
	}

	private void checkMultiBlock(IGridTile currentMultiBlock) {

		if (!currentMultiBlock.isValidForForming()) {
			return;
		}
		currentMultiBlock.onNeighborBlockChange();
		currentMultiBlock.setInvalidForForming();

		IGridTile aBlock;
		for (byte i = 0; i < EnumFacing.VALUES.length; i++) {
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

}
