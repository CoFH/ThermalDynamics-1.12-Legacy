package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.tileentity.TileEntity;

public class DuctUnitStructural extends DuctUnit<DuctUnitStructural, GridStructural<DuctUnitStructural>, Object> {

	public DuctUnitStructural(TileGrid parent) {

		super(parent);
	}

	@Override
	public DuctToken<DuctUnitStructural, GridStructural<DuctUnitStructural>, Object> getToken() {
		return DuctToken.STRUCTURAL;
	}

	@Override
	public GridStructural<DuctUnitStructural> createGrid() {

		return new GridStructural<>(world());
	}

	@Override
	public Object cacheTile(TileEntity tile, byte side) {
		return null;
	}
}
