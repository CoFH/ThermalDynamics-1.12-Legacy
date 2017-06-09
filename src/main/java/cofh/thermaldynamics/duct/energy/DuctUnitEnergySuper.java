package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.TileGrid;

public class DuctUnitEnergySuper extends DuctUnitEnergy {

	public DuctUnitEnergySuper(TileGrid parent, Duct duct, int transferLimit, int capacity) {

		super(parent, duct, transferLimit, capacity);
	}

	@Override
	public GridEnergySuper createGrid() {

		return new GridEnergySuper(parent.world(), getTransferLimit(), getCapacity());
	}

}
