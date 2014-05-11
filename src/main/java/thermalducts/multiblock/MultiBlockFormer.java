package thermalducts.multiblock;

import net.minecraftforge.common.util.ForgeDirection;

public class MultiBlockFormer {

	public void formGrid(IMultiBlock theMultiBlock) {

		theMultiBlock.setInvalidForForming();
		theMultiBlock.setGrid(theMultiBlock.getNewGrid());
		doStep(theMultiBlock.getGrid(), theMultiBlock);

		theMultiBlock.getGrid().resetMultiBlocks();
	}

	public void doStep(MultiBlockGrid theGrid, IMultiBlock currentMultiBlock) {

		IMultiBlock aBlock;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (currentMultiBlock.isSideConnected(i)) {
				aBlock = currentMultiBlock.getConnectedSide(i);
				if (aBlock != null && aBlock.isValidForForming()) {
					if (aBlock.getGrid() != null) {
						if (theGrid.canGridsMerge(aBlock.getGrid())) {
							if (theGrid != aBlock.getGrid()) {
								aBlock.getGrid().mergeGrids(theGrid);
							}
						} else {
							currentMultiBlock.setNotConnected(i);
						}
					} else {
						aBlock.setInvalidForForming();
						aBlock.setGrid(theGrid);
						theGrid.addIdle(aBlock);
						doStep(theGrid, aBlock);
					}
				}
			}
		}
	}
}
