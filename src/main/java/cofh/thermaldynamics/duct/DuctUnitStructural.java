package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.nutypeducts.DuctCache;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.tileentity.TileEntity;

public class DuctUnitStructural extends DuctUnit<DuctUnitStructural, GridStructural<DuctUnitStructural>, DuctCache> {

	public DuctUnitStructural(TileGrid parent) {
		super(parent);
	}

	@Override
	public DuctToken<DuctUnitStructural, GridStructural<DuctUnitStructural>, DuctCache> getToken() {
		return DuctToken.STRUCTURAL;
	}

	@Override
	public GridStructural<DuctUnitStructural> createGrid() {

		return new GridStructural<>(world());
	}

	@Override
	public DuctCache newBlankCache(byte side) {
		return BLANK_CACHE;
	}


	public static DuctCache BLANK_CACHE = new DuctCache() {
		@Override
		public boolean cache(TileEntity tile, byte side) {
			return false;
		}
	};
}
