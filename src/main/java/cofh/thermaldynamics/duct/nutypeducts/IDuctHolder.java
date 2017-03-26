package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import javax.annotation.Nullable;

public interface IDuctHolder {

	@Nullable
	<T extends DuctUnit<T, G, C> & IGridTile, G extends MultiBlockGrid<T>, C extends DuctCache> DuctUnit<T, G, C> getDuct(DuctToken<T, G, C> token);

	boolean isSideBlocked(int side);
}
