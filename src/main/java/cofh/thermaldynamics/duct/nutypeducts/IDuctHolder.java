package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import javax.annotation.Nullable;

public interface IDuctHolder {

	@Nullable
	<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token);

	boolean isSideBlocked(int side);

}
