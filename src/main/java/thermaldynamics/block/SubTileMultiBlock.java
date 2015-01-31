package thermaldynamics.block;

import cofh.lib.util.helpers.ServerHelper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import thermaldynamics.core.TickHandler;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockFormer;
import thermaldynamics.multiblock.MultiBlockGrid;

public abstract class SubTileMultiBlock implements IMultiBlock {

	public MultiBlockGrid grid;
	private boolean isValid = true;
	public TileMultiBlock parent;

	public SubTileMultiBlock(TileMultiBlock parent) {

		this.parent = parent;
	}

	@Override
	public World world() {

		return parent.world();
	}

	@Override
	public int x() {

		return parent.xCoord;
	}

	@Override
	public int y() {

		return parent.yCoord + 1;
	}

	@Override
	public int z() {

		return parent.zCoord;
	}

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
	public abstract MultiBlockGrid getNewGrid();

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		grid = newGrid;
	}

	@Override
	public MultiBlockGrid getGrid() {

		return grid;
	}

	@Override
	public IMultiBlock getConnectedSide(byte side) {

		IMultiBlock connectedSide = parent.getConnectedSide(side);

		if (connectedSide.getClass() != parent.getClass()) {
			return null;
		}

		IMultiBlock[] subTiles = connectedSide.getSubTiles();
		if (subTiles != null) {
			for (IMultiBlock block : subTiles) {
				if (sameType(block)) {
					return block;
				}
			}
		}

		return null;
	}

	@Override
	public boolean isBlockedSide(int side) {

		return parent.isBlockedSide(side);
	}

	public boolean sameType(IMultiBlock other) {

		return other.getClass() == this.getClass();
	}

	@Override
	public boolean isSideConnected(byte side) {

		return parent.isSideConnected(side) && getConnectedSide(side) != null;
	}

	@Override
	public void setNotConnected(byte side) {

	}

	@Override
	public void tickMultiBlock() {

		if (grid == null && ServerHelper.isServerWorld(parent.world())) {
			onNeighbourChange();
			formGrid();
		}
	}

	public void formGrid() {

		if (grid == null && ServerHelper.isServerWorld(parent.world())) {
			new MultiBlockFormer().formGrid(this);
		}
	}

	@Override
	public boolean tickPass(int pass) {

		return false;
	}

	@Override
	public boolean isNode() {

		return false;
	}

	public void tileUnloading() {

	}

	@Override
	public boolean existsYet() {

		return parent.existsYet();
	}

	public void readFromNBT(NBTTagCompound tag) {

		TickHandler.addMultiBlockToCalculate(this);
	}

	public void writeToNBT(NBTTagCompound tag) {

	}

	public final static IMultiBlock[] BLANK = new IMultiBlock[0];

	@Override
	public final IMultiBlock[] getSubTiles() {

		return BLANK;
	}

	public void onChunkUnload() {

		if (grid != null) {
			tileUnloading();
			grid.removeBlock(this);
		}
	}

	public void invalidate() {

		if (grid != null) {
			grid.removeBlock(this);
		}
	}

	public void onNeighbourChange() {

	}

	public void destroyAndRecreate() {

		if (grid != null) {
			grid.destroyAndRecreate();
		}
	}

}
