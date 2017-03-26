package cofh.thermaldynamics.duct.nutypeducts;

import net.minecraft.tileentity.TileEntity;

public abstract class DuctCache {

	public abstract boolean cache(TileEntity tile, byte side);

	public boolean isNode() {

		return true;
	}

}
