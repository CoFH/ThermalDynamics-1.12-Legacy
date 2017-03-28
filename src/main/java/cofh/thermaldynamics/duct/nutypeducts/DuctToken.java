package cofh.thermaldynamics.duct.nutypeducts;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.duct.GridStructural;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.ItemGrid;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class DuctToken<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> {
	// Structure grid, for redstone signals relays
	public static final DuctToken<DuctUnitStructural, GridStructural<DuctUnitStructural>, Object> STRUCTURAL = new DuctToken<>("Structural");

	// Energy grid, for any energy transfer
	public static final DuctToken<? extends DuctUnitEnergy<?, EnergyGrid<?>>, ? extends EnergyGrid<?>, IEnergyReceiver> ENERGY = new DuctToken<>("Energy");

	// 'Local' energy grid, since multiple transfer rates are now on the same large grid, these smaller grids help us track which transfer rate to use.

	// Storage energy grid, for storing internal energy

	// 'Super' energy duct, for large amounts of energy

	// Item grid
	public static final DuctToken<DuctUnitItem, ItemGrid, DuctUnitItem.Cache> ITEMS = new DuctToken<>("Item");

	final String key;

	public DuctToken(String key) {
		this.key = key;
	}
}
