package thermalducts.block;

import cofh.util.BlockHelper;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import thermalducts.multiblock.IMultiBlock;
import thermalducts.multiblock.MultiBlockGrid;

public class TileMultiBlock extends TileEntity implements IMultiBlock {

	public boolean isValid = true;
	public MultiBlockGrid myGrid;
	public IMultiBlock neighborMultiBlocks[] = new IMultiBlock[ForgeDirection.VALID_DIRECTIONS.length];
	public NeighborTypes neighborTypes[] = { NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE };

	@Override
	public void setInvalidForForming() {

		isValid = false;
	}

	@Override
	public void setValidForForming() {

		isValid = true;
	}

	@Override
	public boolean isValidForForming() {

		return isValid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		myGrid = new MultiBlockGrid();
		return myGrid;
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		myGrid = newGrid;
	}

	@Override
	public MultiBlockGrid getGrid() {

		return myGrid;
	}

	@Override
	public IMultiBlock getConnectedSide(byte side) {

		return (IMultiBlock) BlockHelper.getAdjacentTileEntity(this, side);
	}

	@Override
	public boolean isSideConnected(byte side) {

		return BlockHelper.getAdjacentTileEntity(this, side) instanceof TileMultiBlock;
	}

	@Override
	public void setNotConnected(byte side) {

	}

	public void neighborChanged() {

		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {

		}
	}

	public void neighborChanged(int tileX, int tileY, int tileZ) {

	}

	public static enum NeighborTypes {
		NONE, MULTIBLOCK, BLOCK, TILE
	}

}
