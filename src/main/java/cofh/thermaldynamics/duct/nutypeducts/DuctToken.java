package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.duct.GridStructural;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class DuctToken<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> {

	public static final DuctToken<DuctUnitStructural, GridStructural<DuctUnitStructural>, Object> STRUCTURAL = new DuctToken<>();
}
