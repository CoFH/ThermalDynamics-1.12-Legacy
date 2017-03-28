package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.ISingleTick;
import cofh.thermaldynamics.multiblock.MultiBlockFormer2;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class DuctUnit<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> implements IGridTile<T, G>, ISingleTick {

	@SuppressWarnings("unchecked")
	protected final C[] tileCaches = (C[]) new Object[6];
	@SuppressWarnings("unchecked")
	protected final T[] pipeCache = (T[]) new DuctUnit[6];

	protected final TileGrid parent;

	@Nullable
	protected G grid;

	private boolean isValidForForming;
	private byte nodeMask;

	public DuctUnit(TileGrid parent) {

		this.parent = parent;
	}

	public abstract DuctToken<T, G, C> getToken();

	@Nullable
	public G getGrid() {

		return grid;
	}

	@Override
	public void setGrid(@Nullable G newGrid) {

		grid = newGrid;
	}

	public abstract G createGrid();

	public void setInvalidForForming() {

		isValidForForming = false;
	}

	public void setValidForForming() {

		isValidForForming = true;
	}

	public boolean isValidForForming() {

		return isValidForForming;
	}

	public T getConnectedSide(int side) {

		return pipeCache[side];
	}


	@Override
	public void addRelays() {

	}

	public void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull cofh.thermaldynamics.duct.ConnectionType type) {

		nodeMask &= ~(1 << side);

		if (tile == null || !type.allowTransfer) {
			setSideToNone(side);
			return;
		} else if (holder != null) {
			if (holder.isSideBlocked(side ^ 1)) {
				setSideToNone(side);
				return;
			}
			DuctUnit<T, G, C> adjDuct = holder.getDuct(getToken());
			if (adjDuct != null && canConnectToOtherDuct(adjDuct, side)) {
				pipeCache[side] = adjDuct.cast();
			} else {
				setSideToNone(side);
			}
			return;
		}

		loadSignificantCache(tile, side);
	}

	protected boolean canConnectToOtherDuct(DuctUnit<T, G, C> adjDuct, byte side) {

		return true;
	}

	protected void setSideToNone(byte side) {

		pipeCache[side] = null;
		clearCache(side);
	}

	public boolean loadSignificantCache(TileEntity tile, byte side) {

		if (tile == null) {
			tileCaches[side] = null;
			return false;
		}

		C c = cacheTile(tile, side);
		if (c != null) {
			tileCaches[side] = c;
			if (isNode(c)) {
				nodeMask |= (1 << side);
			}

			return true;
		} else {
			tileCaches[side] = null;
			return false;
		}
	}

	@Nullable
	public abstract C cacheTile(@Nonnull TileEntity tile, byte side);

	public boolean isNode(C cache) {
		return true;
	}

	public void clearCache(byte side) {

		tileCaches[side] = null;
	}

	@OverridingMethodsMustInvokeSuper
	public void onChunkUnload() {

		if (grid != null) {
			grid.removeBlock(this.cast());
		}
	}

	@OverridingMethodsMustInvokeSuper
	public void invalidate() {

		if (grid != null) {
			grid.removeBlock(this.cast());
		}
	}

	public void updateSide(TileEntity tile, IDuctHolder holder, byte side) {

		boolean nodeState = nodeMask == 0;
		handleTileSideUpdate(tile, holder, side, parent.getConnectionType(side));
		if (nodeMask == 0 != nodeState && grid != null) {
			grid.addBlock(this.cast());
		}
	}

	public void updateAllSides(TileEntity[] tiles, IDuctHolder[] holders) {

		boolean nodeState = nodeMask == 0;
		for (byte side = 0; side < 6; side++) {
			handleTileSideUpdate(tiles[side], holders[side], side, parent.getConnectionType(side));
		}
		if (nodeMask == 0 != nodeState && grid != null) {
			grid.addBlock(this.cast());
		}
	}

	@SuppressWarnings ("unchecked")
	public final T cast() {

		return (T) this;
	}

	public void formGrid() {

		if (grid != null) {
			MultiBlockFormer2<T, G, C> multiBlockFormer = new MultiBlockFormer2<>();
			multiBlockFormer.formGrid(this.cast());
		}
	}

	@Override
	public boolean isBlockedSide(int side) {

		return parent.isSideBlocked(side);
	}

	@Override
	public World world() {
		return parent.getWorld();
	}

	@Override
	public BlockPos pos() {

		return parent.getPos();
	}

	@Override
	public boolean isNode() {

		return nodeMask != 0;
	}

	@Override
	public boolean isSideConnected(byte side) {

		return false;
	}

	@Override
	public void onNeighborBlockChange() {

		parent.onNeighborBlockChange();
	}

	public void tileUnloading() {

	}

	@OverridingMethodsMustInvokeSuper
	public boolean tickPass(int pass) {
		return !parent.checkForChunkUnload();
	}

	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Nullable
	public <Cap> Cap getCapability(Capability<Cap> capability, EnumFacing from){
		return null;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return nbt;
	}

	@Nullable
	public  NBTTagCompound saveToNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt = writeToNBT(nbt);
		return nbt;
	}

	public byte tickInternalSideCounter(int start) {

		for (byte a = (byte)start; a < 6; a++) {
			if (tileCaches[a] != null) {
				return a;
			}
		}
		for (byte a = 0; a < start; a++) {
			if (tileCaches[a] != null) {
				return a;
			}
		}
		return 0;
	}

	public void onConnectionRejected(int i) {

		pipeCache[i] = null;
	}

	@Override
	public boolean existsYet() {

		return parent.getWorld() != null && parent.getWorld().isBlockLoaded(parent.getPos()) && parent.getWorld().getBlockState(parent.getPos()).getBlock() instanceof BlockDuct;
	}

	@Override
	public boolean isOutdated() {
		return parent.isInvalid();
	}

	@Override
	public void singleTick() {
		if (parent.isInvalid()) {
			return;
		}

		onNeighborBlockChange();

		formGrid();
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeTile(C cacheValue, int side){
		return BlockDuct.ConnectionType.TILECONNECTION;
	}

	@Nonnull
	public BlockDuct.ConnectionType getConnectionType(int side) {
		if(tileCaches[side] != null){
			return getConnectionTypeTile(tileCaches[side], side);
		} else if (pipeCache[side] != null){
			return getConnectionTypeDuct(pipeCache[side], side);
		}
		return BlockDuct.ConnectionType.NONE;
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeDuct(T duct, int side) {
		return BlockDuct.ConnectionType.DUCT;
	}

	public int getLightValue() {
		return 0;
	}

}
