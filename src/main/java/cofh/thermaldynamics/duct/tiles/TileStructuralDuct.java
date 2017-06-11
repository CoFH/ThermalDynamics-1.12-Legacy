package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import javax.annotation.Nullable;
import java.util.Collections;

public class TileStructuralDuct extends TileGrid {

	DuctUnitStructural structural = new DuctUnitStructural(this, TDDucts.structure);

	@Nullable
	@Override
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token) {

		if (token == DuctToken.STRUCTURAL) {
			return (T) structural;
		}
		return null;
	}

	@Override
	public Iterable<DuctUnit> getDuctUnits() {

		return Collections.singleton(structural);
	}

	@Override
	public Duct getDuctType() {

		return TDDucts.structure;
	}
}
