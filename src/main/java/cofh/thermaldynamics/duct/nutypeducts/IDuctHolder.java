package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface IDuctHolder {

	@Nullable
	<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token);

	boolean isSideBlocked(int side);

	@Nullable
	static <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getTokenFromTile(TileEntity tile, DuctToken<T, G, C> token) {

		if (tile instanceof IDuctHolder) {
			return ((IDuctHolder) tile).getDuct(token);
		}
		return null;
	}
}
