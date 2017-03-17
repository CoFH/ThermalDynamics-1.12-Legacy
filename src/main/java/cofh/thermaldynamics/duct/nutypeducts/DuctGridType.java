package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class DuctGridType<T extends DuctUnit<T, G, C> & IGridTile, G extends MultiBlockGrid<T>, C extends DuctCache> {

}
