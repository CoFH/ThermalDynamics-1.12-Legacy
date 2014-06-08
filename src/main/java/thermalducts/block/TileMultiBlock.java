package thermalducts.block;

import cofh.api.tileentity.IPlacedTile;
import cofh.block.TileCoFHBase;
import cofh.util.BlockHelper;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import thermalducts.multiblock.IMultiBlock;
import thermalducts.multiblock.MultiBlockGrid;

public class TileMultiBlock extends TileCoFHBase implements IMultiBlock, IPlacedTile {

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

	@Override
	public void tilePlaced() {

		onNeighborBlockChange();
	}

	@Override
	public void onNeighborBlockChange() {

		TileEntity theTile;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			theTile = BlockHelper.getAdjacentTileEntity(this, i);
			if (isConnectable(theTile)) {
				neighborMultiBlocks[i] = (IMultiBlock) theTile;
				neighborTypes[i] = NeighborTypes.MULTIBLOCK;
			} else if (isSignificantTile(theTile)) {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.TILE;
			} else {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.NONE;
			}
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void onNeighborTileChange(int tileX, int tileY, int tileZ) {

		int side = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);
		TileEntity theTile = worldObj.getTileEntity(tileX, tileY, tileZ);
		if (isConnectable(theTile)) {
			neighborMultiBlocks[side] = (IMultiBlock) theTile;
			neighborTypes[side] = NeighborTypes.MULTIBLOCK;
		} else if (isSignificantTile(theTile)) {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.TILE;
		} else {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.NONE;
		}
	}

	/*
	 * Should return true if theTile is an instance of this multiblock.
	 * 
	 * This must also be an instance of IMultiBlock
	 */
	public boolean isConnectable(TileEntity theTile) {

		return theTile instanceof TileMultiBlock;
	}

	/*
	 * Should return true if theTile is significant to this multiblock
	 * 
	 * IE: Inventory's to ItemDuct's
	 */
	public boolean isSignificantTile(TileEntity theTile) {

		return false;
	}

	@Override
	public String getName() {

		return "tile.thermalducts.multiblock.name";
	}

	@Override
	public int getType() {

		return 0;
	}

	public static enum NeighborTypes {
		NONE, MULTIBLOCK, TILE
	}

}
