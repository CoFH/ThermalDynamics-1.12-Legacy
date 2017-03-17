package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DuctUnit<T extends DuctUnit<T, G, C> & IGridTile, G extends MultiBlockGrid<T>, C extends DuctCache> {

	@SuppressWarnings("unchecked")
	final C[] tileCaches = (C[]) new DuctCache[6];
	@SuppressWarnings("unchecked")
	final T[] pipeCaches = (T[]) new DuctUnit[6];
	final TileDuctBase.NeighborTypes[] neighbourTypes = new TileDuctBase.NeighborTypes[6];
	@Nullable
	G grid;
	boolean isValidForForming;

	public abstract DuctGridType<T, G, C> getToken();

	@Nullable
	public G getGrid() {
		return grid;
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

	public T getConnectedSide(byte side) {
		return pipeCaches[side];
	}

	public abstract C newBlankCache(byte side);

	public abstract boolean cacheSignificantTile(@Nonnull C cache, TileEntity tile, byte side);

	public void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull TileDuctBase.ConnectionTypes type) {
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
				neighbourTypes[side] = TileDuctBase.NeighborTypes.OUTPUT;
			}
			return;
		}

		loadSignificantCache(tile, side);
	}

	protected boolean canConnectToOtherDuct(DuctUnit<T, G, C> adjDuct, byte side) {
		return true;
	}

	protected void setSideToNone(byte side) {
		pipeCaches[side] = null;
		clearCache(side);
		neighbourTypes[side] = TileDuctBase.NeighborTypes.NONE;
	}

	public boolean loadSignificantCache(TileEntity tile, byte side) {
		if (tile == null) {
			tileCaches[side] = null;
			return false;
		}
		C pipeCache = tileCaches[side];
		if (pipeCache == null) {
			pipeCache = newBlankCache(side);
			tileCaches[side] = pipeCache;
		}
		if (cacheSignificantTile(pipeCache, tile, side))
			return true;
		else {
			tileCaches[side] = null;
			return false;
		}
	}

	public void clearCache(byte side) {
		tileCaches[side] = null;
	}

}
