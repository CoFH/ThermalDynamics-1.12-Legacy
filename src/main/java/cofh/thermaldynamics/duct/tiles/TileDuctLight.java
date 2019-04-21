package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.light.DuctUnitLight;

public class TileDuctLight extends TileGridStructureBase {

	public TileDuctLight() {
		addDuctUnits(DuctToken.LIGHT, new DuctUnitLight(this, TDDucts.lightDuct));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.LIGHT;
	}

}
