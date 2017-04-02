package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.light.DuctUnitLight;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGridSingle;

public class TileLuxDuct extends TileGridSingle {
	public TileLuxDuct() {
		super(DuctToken.LIGHT, TDDucts.lightDuct);
	}

	@Override
	protected DuctUnit createDuctUnit(DuctToken token, Duct ductType) {
		return new DuctUnitLight(this, ductType);
	}
}
