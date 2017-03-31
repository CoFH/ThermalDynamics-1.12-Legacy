package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;

public class TileItemDuct extends TileGridStructureBase {
	public TileItemDuct(DuctItem ductType) {
		setDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, ductType));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.ITEMS;
	}
}
