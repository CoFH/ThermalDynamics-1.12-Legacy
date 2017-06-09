package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.duct.GridStructural;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.GridEnergy;
import cofh.thermaldynamics.duct.entity.DuctUnitTransportBase;
import cofh.thermaldynamics.duct.entity.GridTransport;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.fluid.GridFluid;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.GridItem;
import cofh.thermaldynamics.duct.light.DuctUnitLight;
import cofh.thermaldynamics.duct.light.GridLight;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import javax.annotation.Nonnull;

public class DuctToken<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> implements Comparable<DuctToken> {

	// Structure grid, for redstone signals relays
	public static final DuctToken<DuctUnitStructural, GridStructural, Void> STRUCTURAL = new DuctToken<>("Structural");

	// Energy grid, for any energy transfer
	public static final DuctToken<DuctUnitEnergy, GridEnergy, IEnergyReceiver> ENERGY = new DuctToken<>("Energy");

	//	public static final DuctToken<DuctUnitEnergy, GridEnergy, IEnergyReceiver> ENERGY_STORAGE = new DuctToken<>("Energy_Storage");

	// Storage energy grid, for storing internal energy

	// 'Super' energy duct, for large amounts of energy

	// Item grid
	public static final DuctToken<DuctUnitItem, GridItem, DuctUnitItem.Cache> ITEMS = new DuctToken<>("Item");

	// Fluid grid
	public static final DuctToken<DuctUnitFluid, GridFluid, DuctUnitFluid.Cache> FLUID = new DuctToken<>("Fluid");

	public static final DuctToken<DuctUnitTransportBase, GridTransport, DuctUnitTransportBase.TransportDestination> TRANSPORT = new DuctToken<>("Transport");

	public static final DuctToken<DuctUnitLight, GridLight, Void> LIGHT = new DuctToken<>("Light");

	public final static DuctToken[] TOKENS = new DuctToken[] { STRUCTURAL, ENERGY, ITEMS, FLUID, TRANSPORT, LIGHT };

	static {
		for (int i = 0; i < TOKENS.length; i++) {
			TOKENS[i].id = (byte) i;
		}
	}

	public final String key;
	private byte id;

	public DuctToken(String key) {

		this.key = key;
	}

	public byte getId() {

		return id;
	}

	@Override
	public String toString() {

		return "[" + key + "=" + id + "]";
	}

	@Override
	public int compareTo(@Nonnull DuctToken o) {

		return Integer.compare(id, o.id);
	}
}
