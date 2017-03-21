package cofh.thermaldynamics.block;

import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockFormer;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class SubTileGridTile implements IGridTile {

	public MultiBlockGrid grid;
	private boolean isValid = true;
	public TileDuctBase parent;

	public SubTileGridTile(TileDuctBase parent) {

		this.parent = parent;
	}

	@Override
	public World world() {

		return parent.world();
	}

	@Override
	public int x() {

		return parent.x();
	}

	@Override
	public int y() {

		return parent.y() + 1;
	}

	@Override
	public int z() {

		return parent.z();
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
	public abstract MultiBlockGrid createGrid();

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		grid = newGrid;
	}

	@Override
	public MultiBlockGrid getGrid() {

		return grid;
	}

	@Override
	public IGridTile getConnectedSide(byte side) {

		IGridTile connectedSide = parent.getConnectedSide(side);

		if (connectedSide.getClass() != parent.getClass()) {
			return null;
		}

		IGridTile[] subTiles = connectedSide.getSubTiles();
		if (subTiles != null) {
			for (IGridTile block : subTiles) {
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

	public boolean sameType(IGridTile other) {

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
	public void singleTick() {

		if (!parent.isInvalid() && grid == null && ServerHelper.isServerWorld(parent.world())) {
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

	@Override
	public boolean isOutdated() {
		return parent.isOutdated();
	}

	public void readFromNBT(NBTTagCompound tag) {

		TickHandler.addMultiBlockToCalculate(this);
	}

	public void writeToNBT(NBTTagCompound tag) {

	}

	public final static IGridTile[] BLANK = new IGridTile[0];

	@Override
	public final IGridTile[] getSubTiles() {

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

	@Override
	public void addRelays() {

	}

}
