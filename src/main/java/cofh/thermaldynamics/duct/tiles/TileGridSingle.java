package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import javax.annotation.Nullable;
import java.util.Collections;

public abstract class TileGridSingle extends TileGrid {

	final DuctToken token;
	final DuctUnit unit;

	public TileGridSingle(DuctToken token, Duct ductType) {

		this.token = token;
		this.unit = createDuctUnit(token, ductType);
	}

	protected abstract DuctUnit createDuctUnit(DuctToken token, Duct ductType);

	@Nullable
	@Override
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token) {

		if (token == this.token) {
			return (T) unit;
		}
		return null;
	}

	@Override
	public Iterable<DuctUnit> getDuctUnits() {

		return Collections.singleton(unit);
	}

	@Override
	public Duct getDuctType() {

		return unit.getDuctType();
	}
}
